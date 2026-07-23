package com.reservation.sap.service;

import com.reservation.sap.config.SapSyncProperties;
import com.reservation.sap.dto.OddzialDto;
import com.reservation.sap.model.Oddzial;
import com.reservation.sap.repository.OddzialRepository;
import com.reservation.sap.repository.OddzialStagingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OddzialSyncServiceTest {

    @Autowired
    private OddzialSyncService syncService;

    @Autowired
    private OddzialRepository oddzialRepository;

    @Autowired
    private OddzialStagingRepository stagingRepository;

    @Autowired
    private SapSyncProperties properties;

    @BeforeEach
    void setUp() {
        properties.setStrategy(SapSyncProperties.Strategy.UPSERT);
        properties.setObsoleteMode(SapSyncProperties.ObsoleteMode.SOFT_DELETE);
        properties.setObsoleteStatus("NIEAKTUALNY");
        oddzialRepository.deleteAll();
        stagingRepository.deleteAllRows();
    }

    @Test
    void upsertInsertsUpdatesAndSoftDeletesMissing() {
        oddzialRepository.save(sample(1, "A", "AKTUALNY"));
        oddzialRepository.save(sample(2, "B", "AKTUALNY"));

        OddzialDto keepUpdated = dto(1, "A-updated", "AKTUALNY");
        OddzialDto inserted = dto(3, "C", "AKTUALNY");

        OddzialSyncService.Counters counters = syncService.apply(List.of(keepUpdated, inserted));

        assertThat(counters.getReceived()).isEqualTo(2);
        assertThat(counters.getInserted()).isEqualTo(1);
        assertThat(counters.getUpdated()).isEqualTo(1);
        assertThat(counters.getDeactivated()).isEqualTo(1);

        Oddzial one = oddzialRepository.findById(1).orElseThrow();
        assertThat(one.getOddzial()).isEqualTo("A-updated");
        assertThat(one.getTelefon()).isEqualTo("221111111");
        assertThat(one.getRodzajGazu()).isEqualTo("E");

        assertThat(oddzialRepository.findById(2).orElseThrow().getStatusNaStronie()).isEqualTo("NIEAKTUALNY");
        assertThat(oddzialRepository.findById(3)).isPresent();
        assertThat(oddzialRepository.findAllNotObsolete("NIEAKTUALNY")).extracting(Oddzial::getId)
                .containsExactlyInAnyOrder(1, 3);
    }

    @Test
    void stagingSwapPromotesIntoTargetTable() {
        properties.setStrategy(SapSyncProperties.Strategy.STAGING_SWAP);
        oddzialRepository.save(sample(10, "OLD", "AKTUALNY"));

        OddzialSyncService.Counters counters = syncService.apply(List.of(dto(10, "NEW", "AKTUALNY"), dto(11, "X", "AKTUALNY")));

        assertThat(counters.getUpdated()).isEqualTo(1);
        assertThat(counters.getInserted()).isEqualTo(1);
        assertThat(stagingRepository.count()).isEqualTo(2);
        assertThat(oddzialRepository.findById(10).orElseThrow().getOddzial()).isEqualTo("NEW");
        assertThat(oddzialRepository.findById(11)).isPresent();
    }

    @Test
    void failureOnInvalidFeedLeavesExistingData() {
        oddzialRepository.save(sample(5, "SAFE", "AKTUALNY"));

        assertThatThrownBy(() -> syncService.apply(List.of(dto(5, "X", "AKTUALNY"), dto(5, "DUP", "AKTUALNY"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Zduplikowane id");

        // @Transactional test rolls back the failed method; existing row from setUp path:
        // the duplicate validation fails before writes — data untouched
        assertThat(oddzialRepository.findById(5).orElseThrow().getOddzial()).isEqualTo("SAFE");
    }

    @Test
    void requiresId() {
        OddzialDto withoutId = dto(1, "A", "AKTUALNY");
        withoutId.setId(null);
        assertThatThrownBy(() -> syncService.apply(List.of(withoutId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bez id");
    }

    private static Oddzial sample(int id, String name, String status) {
        return Oddzial.builder()
                .id(id)
                .oddzial(name)
                .nazwaMiejscowosci("Miasto")
                .kodPocztowy("00-001")
                .nazwaUlicy("Ulica 1")
                .wojewodztwo("mazowieckie")
                .powiat("powiat")
                .gmina("gmina")
                .rcs(1)
                .wspolczynnikCieplaSpalania(new BigDecimal("10.50"))
                .dataWstawienia(LocalDateTime.of(2026, 1, 1, 12, 0))
                .statusNaStronie(status)
                .telefon("220000000")
                .email("a@example.com")
                .rodzajGazu("E")
                .stopienGazyfikacji("wysoki")
                .punktyWejscia("PW-1")
                .build();
    }

    private static OddzialDto dto(int id, String name, String status) {
        OddzialDto dto = new OddzialDto();
        dto.setId(id);
        dto.setOddzial(name);
        dto.setNazwaMiejscowosci("Miasto");
        dto.setKodPocztowy("00-001");
        dto.setNazwaUlicy("Ulica 1");
        dto.setWojewodztwo("mazowieckie");
        dto.setPowiat("powiat");
        dto.setGmina("gmina");
        dto.setRcs(1);
        dto.setWspolczynnikCieplaSpalania(new BigDecimal("10.50"));
        dto.setDataWstawienia(LocalDateTime.of(2026, 7, 23, 8, 0));
        dto.setStatusNaStronie(status);
        dto.setTelefon("221111111");
        dto.setEmail("oddzial@example.com");
        dto.setRodzajGazu("E");
        dto.setStopienGazyfikacji("wysoki");
        dto.setPunktyWejscia("PW-1");
        return dto;
    }
}
