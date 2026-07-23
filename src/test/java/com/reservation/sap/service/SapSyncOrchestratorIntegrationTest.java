package com.reservation.sap.service;

import com.reservation.config.TestMailConfig;
import com.reservation.sap.client.SapDataClient;
import com.reservation.sap.dto.BranchDto;
import com.reservation.sap.model.Branch;
import com.reservation.sap.repository.BranchRepository;
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
    private BranchRepository branchRepository;

    @Autowired
    private AtomicReference<List<BranchDto>> feedHolder;

    @BeforeEach
    void setUp() {
        branchRepository.deleteAll();
        feedHolder.set(List.of());
    }

    @Test
    void syncEndpointLoadsSnapshotAndKeepsDataOnNextPartialFeed() throws Exception {
        BranchDto first = dto(1, "Branch 1");
        BranchDto second = dto(2, "Branch 2");
        feedHolder.set(List.of(first, second));

        mockMvc.perform(post("/api/sap/sync/branches").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.received").value(2))
                .andExpect(jsonPath("$.inserted").value(2));

        assertThat(branchRepository.findAll()).hasSize(2);

        BranchDto updated = dto(1, "Branch 1 bis");
        feedHolder.set(List.of(updated));

        mockMvc.perform(post("/api/sap/sync/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated").value(1))
                .andExpect(jsonPath("$.deactivated").value(1));

        Branch kept = branchRepository.findById(1).orElseThrow();
        assertThat(kept.getName()).isEqualTo("Branch 1 bis");
        assertThat(branchRepository.findById(2).orElseThrow().getPageStatus()).isEqualTo("NIEAKTUALNY");

        mockMvc.perform(get("/api/sap/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        mockMvc.perform(get("/api/sap/sync/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void failedSyncDoesNotWipeExistingRows() throws Exception {
        branchRepository.save(Branch.builder()
                .id(9)
                .name("Existing")
                .pageStatus("AKTUALNY")
                .insertedAt(LocalDateTime.now())
                .build());

        BranchDto bad = dto(9, "X");
        BranchDto dup = dto(9, "Y");
        feedHolder.set(List.of(bad, dup));

        mockMvc.perform(post("/api/sap/sync/branches"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("FAILED"));

        assertThat(branchRepository.findById(9).orElseThrow().getName()).isEqualTo("Existing");
    }

    private static BranchDto dto(int id, String name) {
        BranchDto dto = new BranchDto();
        dto.setId(id);
        dto.setName(name);
        dto.setLocalityName("City");
        dto.setPostalCode("00-001");
        dto.setStreetName("Street");
        dto.setProvince("mazowieckie");
        dto.setCounty("county");
        dto.setCommune("commune");
        dto.setRcs(0);
        dto.setCombustionHeatCoefficient(new BigDecimal("1.23"));
        dto.setInsertedAt(LocalDateTime.of(2026, 7, 23, 10, 0));
        dto.setPageStatus("AKTUALNY");
        dto.setPhone("221234567");
        dto.setEmail("a@b.pl");
        dto.setGasType("Lw");
        dto.setGasificationDegree("medium");
        dto.setEntryPoints("PW");
        return dto;
    }

    @TestConfiguration
    static class SapClientTestConfig {
        @Bean
        AtomicReference<List<BranchDto>> feedHolder() {
            return new AtomicReference<>(new ArrayList<>());
        }

        @Bean
        @Primary
        SapDataClient sapDataClient(AtomicReference<List<BranchDto>> feedHolder) {
            return () -> List.copyOf(feedHolder.get());
        }
    }
}
