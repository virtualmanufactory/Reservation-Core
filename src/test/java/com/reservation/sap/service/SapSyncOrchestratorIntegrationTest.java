package com.reservation.sap.service;

import com.reservation.config.TestMailConfig;
import com.reservation.sap.client.SapDataClient;
import com.reservation.sap.dto.OddzialDto;
import com.reservation.sap.dto.SapSyncResultDto;
import com.reservation.sap.model.Oddzial;
import com.reservation.sap.model.SapSyncStatus;
import com.reservation.sap.repository.OddzialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestMailConfig.class, SapSyncOrchestratorIntegrationTest.SapClientTestConfig.class})
class SapSyncOrchestratorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OddzialRepository oddzialRepository;

    @Autowired
    private AtomicReference<List<OddzialDto>> feedHolder;

    @BeforeEach
    void setUp() {
        oddzialRepository.deleteAll();
        feedHolder.set(List.of());
    }

    @Test
    void syncEndpointLoadsSnapshotAndKeepsDataOnNextPartialFeed() throws Exception {
        OddzialDto first = dto(1, "Oddział 1");
        OddzialDto second = dto(2, "Oddział 2");
        feedHolder.set(List.of(first, second));

        mockMvc.perform(post("/api/sap/sync/oddzialy").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.received").value(2))
                .andExpect(jsonPath("$.inserted").value(2));

        assertThat(oddzialRepository.findAll()).hasSize(2);

        // Drugi feed bez id=2 → soft-delete
        OddzialDto updated = dto(1, "Oddział 1 bis");
        feedHolder.set(List.of(updated));

        mockMvc.perform(post("/api/sap/sync/oddzialy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated").value(1))
                .andExpect(jsonPath("$.deactivated").value(1));

        Oddzial kept = oddzialRepository.findById(1).orElseThrow();
        assertThat(kept.getOddzial()).isEqualTo("Oddział 1 bis");
        assertThat(oddzialRepository.findById(2).orElseThrow().getStatusNaStronie()).isEqualTo("NIEAKTUALNY");

        mockMvc.perform(get("/api/sap/oddzialy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        mockMvc.perform(get("/api/sap/sync/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void failedSyncDoesNotWipeExistingRows() throws Exception {
        oddzialRepository.save(Oddzial.builder()
                .id(9)
                .oddzial("Istniejący")
                .statusNaStronie("AKTUALNY")
                .dataWstawienia(LocalDateTime.now())
                .build());

        OddzialDto bad = dto(9, "X");
        OddzialDto dup = dto(9, "Y");
        feedHolder.set(List.of(bad, dup));

        mockMvc.perform(post("/api/sap/sync/oddzialy"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("FAILED"));

        assertThat(oddzialRepository.findById(9).orElseThrow().getOddzial()).isEqualTo("Istniejący");
    }

    private static OddzialDto dto(int id, String name) {
        OddzialDto dto = new OddzialDto();
        dto.setId(id);
        dto.setOddzial(name);
        dto.setNazwaMiejscowosci("Miasto");
        dto.setKodPocztowy("00-001");
        dto.setNazwaUlicy("Ulica");
        dto.setWojewodztwo("mazowieckie");
        dto.setPowiat("powiat");
        dto.setGmina("gmina");
        dto.setRcs(0);
        dto.setWspolczynnikCieplaSpalania(new BigDecimal("1.23"));
        dto.setDataWstawienia(LocalDateTime.of(2026, 7, 23, 10, 0));
        dto.setStatusNaStronie("AKTUALNY");
        dto.setTelefon("221234567");
        dto.setEmail("a@b.pl");
        dto.setRodzajGazu("Lw");
        dto.setStopienGazyfikacji("średni");
        dto.setPunktyWejscia("PW");
        return dto;
    }

    @TestConfiguration
    static class SapClientTestConfig {
        @Bean
        AtomicReference<List<OddzialDto>> feedHolder() {
            return new AtomicReference<>(new ArrayList<>());
        }

        @Bean
        @Primary
        SapDataClient sapDataClient(AtomicReference<List<OddzialDto>> feedHolder) {
            return () -> List.copyOf(feedHolder.get());
        }
    }
}
