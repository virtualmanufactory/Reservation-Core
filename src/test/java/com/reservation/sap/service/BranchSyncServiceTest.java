package com.reservation.sap.service;

import com.reservation.sap.config.SapSyncProperties;
import com.reservation.sap.dto.BranchDto;
import com.reservation.sap.model.Branch;
import com.reservation.sap.repository.BranchRepository;
import com.reservation.sap.repository.BranchStagingRepository;
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
class BranchSyncServiceTest {

    @Autowired
    private BranchSyncService syncService;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private BranchStagingRepository stagingRepository;

    @Autowired
    private SapSyncProperties properties;

    @BeforeEach
    void setUp() {
        properties.setStrategy(SapSyncProperties.Strategy.UPSERT);
        properties.setObsoleteMode(SapSyncProperties.ObsoleteMode.SOFT_DELETE);
        properties.setObsoleteStatus("NIEAKTUALNY");
        branchRepository.deleteAll();
        stagingRepository.deleteAllRows();
    }

    @Test
    void upsertInsertsUpdatesAndSoftDeletesMissing() {
        branchRepository.save(sample(1, "A", "AKTUALNY"));
        branchRepository.save(sample(2, "B", "AKTUALNY"));

        BranchDto keepUpdated = dto(1, "A-updated", "AKTUALNY");
        BranchDto inserted = dto(3, "C", "AKTUALNY");

        BranchSyncService.Counters counters = syncService.apply(List.of(keepUpdated, inserted));

        assertThat(counters.getReceived()).isEqualTo(2);
        assertThat(counters.getInserted()).isEqualTo(1);
        assertThat(counters.getUpdated()).isEqualTo(1);
        assertThat(counters.getDeactivated()).isEqualTo(1);

        Branch one = branchRepository.findById(1).orElseThrow();
        assertThat(one.getName()).isEqualTo("A-updated");
        assertThat(one.getPhone()).isEqualTo("221111111");
        assertThat(one.getGasType()).isEqualTo("E");

        assertThat(branchRepository.findById(2).orElseThrow().getPageStatus()).isEqualTo("NIEAKTUALNY");
        assertThat(branchRepository.findById(3)).isPresent();
        assertThat(branchRepository.findAllNotObsolete("NIEAKTUALNY")).extracting(Branch::getId)
                .containsExactlyInAnyOrder(1, 3);
    }

    @Test
    void stagingSwapPromotesIntoTargetTable() {
        properties.setStrategy(SapSyncProperties.Strategy.STAGING_SWAP);
        branchRepository.save(sample(10, "OLD", "AKTUALNY"));

        BranchSyncService.Counters counters = syncService.apply(List.of(dto(10, "NEW", "AKTUALNY"), dto(11, "X", "AKTUALNY")));

        assertThat(counters.getUpdated()).isEqualTo(1);
        assertThat(counters.getInserted()).isEqualTo(1);
        assertThat(stagingRepository.count()).isEqualTo(2);
        assertThat(branchRepository.findById(10).orElseThrow().getName()).isEqualTo("NEW");
        assertThat(branchRepository.findById(11)).isPresent();
    }

    @Test
    void failureOnInvalidFeedLeavesExistingData() {
        branchRepository.save(sample(5, "SAFE", "AKTUALNY"));

        assertThatThrownBy(() -> syncService.apply(List.of(dto(5, "X", "AKTUALNY"), dto(5, "DUP", "AKTUALNY"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate id");

        assertThat(branchRepository.findById(5).orElseThrow().getName()).isEqualTo("SAFE");
    }

    @Test
    void requiresId() {
        BranchDto withoutId = dto(1, "A", "AKTUALNY");
        withoutId.setId(null);
        assertThatThrownBy(() -> syncService.apply(List.of(withoutId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("without id");
    }

    private static Branch sample(int id, String name, String status) {
        return Branch.builder()
                .id(id)
                .name(name)
                .localityName("City")
                .postalCode("00-001")
                .streetName("Street 1")
                .province("mazowieckie")
                .county("county")
                .commune("commune")
                .rcs(1)
                .combustionHeatCoefficient(new BigDecimal("10.50"))
                .insertedAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .pageStatus(status)
                .phone("220000000")
                .email("a@example.com")
                .gasType("E")
                .gasificationDegree("high")
                .entryPoints("PW-1")
                .build();
    }

    private static BranchDto dto(int id, String name, String status) {
        BranchDto dto = new BranchDto();
        dto.setId(id);
        dto.setName(name);
        dto.setLocalityName("City");
        dto.setPostalCode("00-001");
        dto.setStreetName("Street 1");
        dto.setProvince("mazowieckie");
        dto.setCounty("county");
        dto.setCommune("commune");
        dto.setRcs(1);
        dto.setCombustionHeatCoefficient(new BigDecimal("10.50"));
        dto.setInsertedAt(LocalDateTime.of(2026, 7, 23, 8, 0));
        dto.setPageStatus(status);
        dto.setPhone("221111111");
        dto.setEmail("branch@example.com");
        dto.setGasType("E");
        dto.setGasificationDegree("high");
        dto.setEntryPoints("PW-1");
        return dto;
    }
}
