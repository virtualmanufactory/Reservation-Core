package com.reservation.sap.service;

import com.reservation.sap.config.SapSyncProperties;
import com.reservation.sap.dto.OddzialDto;
import com.reservation.sap.model.Oddzial;
import com.reservation.sap.model.OddzialStaging;
import com.reservation.sap.repository.OddzialRepository;
import com.reservation.sap.repository.OddzialStagingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Zasilenie tabeli {@code oddzial} danymi z SAP bez truncate+reload.
 * Przy błędzie transakcja jest wycofywana — dotychczasowe poprawne dane zostają dostępne.
 */
@Service
public class OddzialSyncService {

    private static final Logger log = LoggerFactory.getLogger(OddzialSyncService.class);

    private final OddzialRepository oddzialRepository;
    private final OddzialStagingRepository stagingRepository;
    private final SapSyncProperties properties;

    public OddzialSyncService(
            OddzialRepository oddzialRepository,
            OddzialStagingRepository stagingRepository,
            SapSyncProperties properties) {
        this.oddzialRepository = oddzialRepository;
        this.stagingRepository = stagingRepository;
        this.properties = properties;
    }

    @Transactional
    public Counters apply(List<OddzialDto> incoming) {
        List<OddzialDto> rows = validate(incoming);
        return switch (properties.getStrategy()) {
            case UPSERT -> applyUpsert(rows);
            case STAGING_SWAP -> applyStagingSwap(rows);
        };
    }

    private Counters applyUpsert(List<OddzialDto> incoming) {
        log.info("SAP UPSERT oddzial: {} rekordów", incoming.size());
        Counters counters = new Counters();
        counters.received = incoming.size();

        if (incoming.isEmpty()) {
            counters.merge(handleEmptySnapshot());
            return counters;
        }

        Set<Integer> ids = incoming.stream()
                .map(OddzialDto::getId)
                .collect(Collectors.toCollection(HashSet::new));

        Map<Integer, Oddzial> existing = oddzialRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Oddzial::getId, Function.identity()));

        List<Oddzial> toSave = new ArrayList<>(incoming.size());
        for (OddzialDto dto : incoming) {
            Oddzial entity = existing.get(dto.getId());
            if (entity == null) {
                entity = Oddzial.builder().id(dto.getId()).build();
                counters.inserted++;
            } else {
                counters.updated++;
            }
            copy(dto, entity);
            toSave.add(entity);
        }
        oddzialRepository.saveAll(toSave);

        applyObsolete(ids, counters);

        log.info(
                "SAP UPSERT oddzial zakończony: inserted={}, updated={}, deactivated={}, deleted={}",
                counters.inserted, counters.updated, counters.deactivated, counters.deleted);
        return counters;
    }

    private Counters applyStagingSwap(List<OddzialDto> incoming) {
        log.info("SAP STAGING_SWAP oddzial: ładowanie {} rekordów do staging", incoming.size());
        Counters counters = new Counters();
        counters.received = incoming.size();

        stagingRepository.deleteAllRows();
        List<OddzialStaging> stagingRows = incoming.stream()
                .map(dto -> OddzialStaging.builder()
                        .sourceId(dto.getId())
                        .oddzial(dto.getOddzial())
                        .nazwaMiejscowosci(dto.getNazwaMiejscowosci())
                        .kodPocztowy(dto.getKodPocztowy())
                        .nazwaUlicy(dto.getNazwaUlicy())
                        .wojewodztwo(dto.getWojewodztwo())
                        .powiat(dto.getPowiat())
                        .gmina(dto.getGmina())
                        .rcs(dto.getRcs())
                        .wspolczynnikCieplaSpalania(dto.getWspolczynnikCieplaSpalania())
                        .dataWstawienia(dto.getDataWstawienia() != null ? dto.getDataWstawienia() : LocalDateTime.now())
                        .statusNaStronie(dto.getStatusNaStronie())
                        .telefon(dto.getTelefon())
                        .email(dto.getEmail())
                        .rodzajGazu(dto.getRodzajGazu())
                        .stopienGazyfikacji(dto.getStopienGazyfikacji())
                        .punktyWejscia(dto.getPunktyWejscia())
                        .build())
                .toList();
        stagingRepository.saveAll(stagingRows);
        stagingRepository.flush();

        // Promocja do tabeli docelowej w tej samej transakcji.
        return applyUpsert(incoming);
    }

    private void applyObsolete(Set<Integer> ids, Counters counters) {
        if (properties.getObsoleteMode() == SapSyncProperties.ObsoleteMode.SOFT_DELETE) {
            counters.deactivated = oddzialRepository.markObsoleteMissing(ids, properties.getObsoleteStatus());
        } else {
            counters.deleted = oddzialRepository.deleteMissing(ids);
        }
    }

    private Counters handleEmptySnapshot() {
        Counters counters = new Counters();
        log.warn("SAP feed pusty — oznaczam/usuwam wszystkie oddziały jako nieaktualne");
        if (properties.getObsoleteMode() == SapSyncProperties.ObsoleteMode.SOFT_DELETE) {
            counters.deactivated = oddzialRepository.markAllObsolete(properties.getObsoleteStatus());
        } else {
            counters.deleted = oddzialRepository.deleteAllRows();
        }
        return counters;
    }

    private List<OddzialDto> validate(List<OddzialDto> incoming) {
        if (incoming == null) {
            throw new IllegalArgumentException("Lista oddziałów SAP nie może być null");
        }
        Set<Integer> seenIds = new HashSet<>();
        for (OddzialDto dto : incoming) {
            if (dto == null) {
                throw new IllegalArgumentException("Element feedu SAP jest null");
            }
            if (dto.getId() == null) {
                throw new IllegalArgumentException("Rekord SAP bez id (wymagane do UPSERT)");
            }
            if (!seenIds.add(dto.getId())) {
                throw new IllegalArgumentException("Zduplikowane id w feedzie SAP: " + dto.getId());
            }
        }
        return incoming;
    }

    private void copy(OddzialDto dto, Oddzial entity) {
        entity.setId(dto.getId());
        entity.setOddzial(dto.getOddzial());
        entity.setNazwaMiejscowosci(dto.getNazwaMiejscowosci());
        entity.setKodPocztowy(dto.getKodPocztowy());
        entity.setNazwaUlicy(dto.getNazwaUlicy());
        entity.setWojewodztwo(dto.getWojewodztwo());
        entity.setPowiat(dto.getPowiat());
        entity.setGmina(dto.getGmina());
        entity.setRcs(dto.getRcs());
        entity.setWspolczynnikCieplaSpalania(dto.getWspolczynnikCieplaSpalania());
        entity.setDataWstawienia(dto.getDataWstawienia() != null ? dto.getDataWstawienia() : LocalDateTime.now());
        entity.setStatusNaStronie(dto.getStatusNaStronie());
        entity.setTelefon(dto.getTelefon());
        entity.setEmail(dto.getEmail());
        entity.setRodzajGazu(dto.getRodzajGazu());
        entity.setStopienGazyfikacji(dto.getStopienGazyfikacji());
        entity.setPunktyWejscia(dto.getPunktyWejscia());
    }

    public static final class Counters {
        private int received;
        private int inserted;
        private int updated;
        private int deactivated;
        private int deleted;

        private void merge(Counters other) {
            this.deactivated += other.deactivated;
            this.deleted += other.deleted;
        }

        public int getReceived() {
            return received;
        }

        public int getInserted() {
            return inserted;
        }

        public int getUpdated() {
            return updated;
        }

        public int getDeactivated() {
            return deactivated;
        }

        public int getDeleted() {
            return deleted;
        }
    }
}
