package com.reservation.sap.web;

import com.reservation.sap.config.SapSyncProperties;
import com.reservation.sap.dto.SapSyncResultDto;
import com.reservation.sap.model.Branch;
import com.reservation.sap.repository.BranchRepository;
import com.reservation.sap.service.SapSyncException;
import com.reservation.sap.service.SapSyncOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sap")
public class SapSyncController {

    private final SapSyncOrchestrator orchestrator;
    private final BranchRepository branchRepository;
    private final SapSyncProperties properties;

    public SapSyncController(
            SapSyncOrchestrator orchestrator,
            BranchRepository branchRepository,
            SapSyncProperties properties) {
        this.orchestrator = orchestrator;
        this.branchRepository = branchRepository;
        this.properties = properties;
    }

    @PostMapping("/sync/branches")
    public ResponseEntity<?> syncBranches() {
        try {
            return ResponseEntity.ok(orchestrator.syncBranches());
        } catch (SapSyncException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getResult());
        }
    }

    @GetMapping("/sync/runs")
    public List<SapSyncResultDto> recentRuns() {
        return orchestrator.recentRuns();
    }

    @GetMapping("/branches")
    public List<Branch> listBranches() {
        return branchRepository.findAllNotObsolete(properties.getObsoleteStatus());
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "module", "sap-sync", "table", "oddzial");
    }
}
