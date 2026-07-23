package com.reservation.sap.service;

import com.reservation.sap.config.SapSyncProperties;
import com.reservation.sap.dto.BranchDto;
import com.reservation.sap.model.Branch;
import com.reservation.sap.model.BranchStaging;
import com.reservation.sap.repository.BranchRepository;
import com.reservation.sap.repository.BranchStagingRepository;
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
 * Loads SAP snapshot into MySQL table {@code oddzial} without truncate+reload.
 * On failure the transaction rolls back and previous data stays available.
 */
@Service
public class BranchSyncService {

    private static final Logger log = LoggerFactory.getLogger(BranchSyncService.class);

    private final BranchRepository branchRepository;
    private final BranchStagingRepository stagingRepository;
    private final SapSyncProperties properties;

    public BranchSyncService(
            BranchRepository branchRepository,
            BranchStagingRepository stagingRepository,
            SapSyncProperties properties) {
        this.branchRepository = branchRepository;
        this.stagingRepository = stagingRepository;
        this.properties = properties;
    }

    @Transactional
    public Counters apply(List<BranchDto> incoming) {
        List<BranchDto> rows = validate(incoming);
        return switch (properties.getStrategy()) {
            case UPSERT -> applyUpsert(rows);
            case STAGING_SWAP -> applyStagingSwap(rows);
        };
    }

    private Counters applyUpsert(List<BranchDto> incoming) {
        log.info("SAP UPSERT branch: {} rows", incoming.size());
        Counters counters = new Counters();
        counters.received = incoming.size();

        if (incoming.isEmpty()) {
            counters.merge(handleEmptySnapshot());
            return counters;
        }

        Set<Integer> ids = incoming.stream()
                .map(BranchDto::getId)
                .collect(Collectors.toCollection(HashSet::new));

        Map<Integer, Branch> existing = branchRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Branch::getId, Function.identity()));

        List<Branch> toSave = new ArrayList<>(incoming.size());
        for (BranchDto dto : incoming) {
            Branch entity = existing.get(dto.getId());
            if (entity == null) {
                entity = Branch.builder().id(dto.getId()).build();
                counters.inserted++;
            } else {
                counters.updated++;
            }
            copy(dto, entity);
            toSave.add(entity);
        }
        branchRepository.saveAll(toSave);

        applyObsolete(ids, counters);

        log.info(
                "SAP UPSERT branch finished: inserted={}, updated={}, deactivated={}, deleted={}",
                counters.inserted, counters.updated, counters.deactivated, counters.deleted);
        return counters;
    }

    private Counters applyStagingSwap(List<BranchDto> incoming) {
        log.info("SAP STAGING_SWAP branch: loading {} rows into staging", incoming.size());
        Counters counters = new Counters();
        counters.received = incoming.size();

        stagingRepository.deleteAllRows();
        List<BranchStaging> stagingRows = incoming.stream()
                .map(dto -> BranchStaging.builder()
                        .sourceId(dto.getId())
                        .name(dto.getName())
                        .localityName(dto.getLocalityName())
                        .postalCode(dto.getPostalCode())
                        .streetName(dto.getStreetName())
                        .province(dto.getProvince())
                        .county(dto.getCounty())
                        .commune(dto.getCommune())
                        .rcs(dto.getRcs())
                        .combustionHeatCoefficient(dto.getCombustionHeatCoefficient())
                        .insertedAt(dto.getInsertedAt() != null ? dto.getInsertedAt() : LocalDateTime.now())
                        .pageStatus(dto.getPageStatus())
                        .phone(dto.getPhone())
                        .email(dto.getEmail())
                        .gasType(dto.getGasType())
                        .gasificationDegree(dto.getGasificationDegree())
                        .entryPoints(dto.getEntryPoints())
                        .build())
                .toList();
        stagingRepository.saveAll(stagingRows);
        stagingRepository.flush();

        return applyUpsert(incoming);
    }

    private void applyObsolete(Set<Integer> ids, Counters counters) {
        if (properties.getObsoleteMode() == SapSyncProperties.ObsoleteMode.SOFT_DELETE) {
            counters.deactivated = branchRepository.markObsoleteMissing(ids, properties.getObsoleteStatus());
        } else {
            counters.deleted = branchRepository.deleteMissing(ids);
        }
    }

    private Counters handleEmptySnapshot() {
        Counters counters = new Counters();
        log.warn("SAP feed empty — marking/removing all branches as obsolete");
        if (properties.getObsoleteMode() == SapSyncProperties.ObsoleteMode.SOFT_DELETE) {
            counters.deactivated = branchRepository.markAllObsolete(properties.getObsoleteStatus());
        } else {
            counters.deleted = branchRepository.deleteAllRows();
        }
        return counters;
    }

    private List<BranchDto> validate(List<BranchDto> incoming) {
        if (incoming == null) {
            throw new IllegalArgumentException("SAP branch list must not be null");
        }
        Set<Integer> seenIds = new HashSet<>();
        for (BranchDto dto : incoming) {
            if (dto == null) {
                throw new IllegalArgumentException("SAP feed element is null");
            }
            if (dto.getId() == null) {
                throw new IllegalArgumentException("SAP record without id (required for UPSERT)");
            }
            if (!seenIds.add(dto.getId())) {
                throw new IllegalArgumentException("Duplicate id in SAP feed: " + dto.getId());
            }
        }
        return incoming;
    }

    private void copy(BranchDto dto, Branch entity) {
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setLocalityName(dto.getLocalityName());
        entity.setPostalCode(dto.getPostalCode());
        entity.setStreetName(dto.getStreetName());
        entity.setProvince(dto.getProvince());
        entity.setCounty(dto.getCounty());
        entity.setCommune(dto.getCommune());
        entity.setRcs(dto.getRcs());
        entity.setCombustionHeatCoefficient(dto.getCombustionHeatCoefficient());
        entity.setInsertedAt(dto.getInsertedAt() != null ? dto.getInsertedAt() : LocalDateTime.now());
        entity.setPageStatus(dto.getPageStatus());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setGasType(dto.getGasType());
        entity.setGasificationDegree(dto.getGasificationDegree());
        entity.setEntryPoints(dto.getEntryPoints());
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
