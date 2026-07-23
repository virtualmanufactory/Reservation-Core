package com.reservation.sap.service;

import com.reservation.sap.client.SapDataClient;
import com.reservation.sap.config.SapSyncProperties;
import com.reservation.sap.dto.OddzialDto;
import com.reservation.sap.dto.SapSyncResultDto;
import com.reservation.sap.model.SapSyncRunLog;
import com.reservation.sap.model.SapSyncStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Orkiestracja zasilenia SAP: pobranie → UPSERT/staging → log audytowy.
 */
@Service
public class SapSyncOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SapSyncOrchestrator.class);
    private static final String DATASET_ODDZIAL = "ODDZIAL";

    private final SapDataClient sapDataClient;
    private final OddzialSyncService oddzialSyncService;
    private final SapSyncAuditService auditService;
    private final SapSyncProperties properties;

    public SapSyncOrchestrator(
            SapDataClient sapDataClient,
            OddzialSyncService oddzialSyncService,
            SapSyncAuditService auditService,
            SapSyncProperties properties) {
        this.sapDataClient = sapDataClient;
        this.oddzialSyncService = oddzialSyncService;
        this.auditService = auditService;
        this.properties = properties;
    }

    public SapSyncResultDto syncOddzialy() {
        Instant startedAt = Instant.now();
        String strategy = properties.getStrategy().name();
        log.info("SAP sync started (dataset={}, strategy={})", DATASET_ODDZIAL, strategy);

        try {
            List<OddzialDto> rows = sapDataClient.fetchOddzialy();
            OddzialSyncService.Counters counters = oddzialSyncService.apply(rows);

            SapSyncRunLog runLog = auditService.save(SapSyncRunLog.builder()
                    .dataset(DATASET_ODDZIAL)
                    .strategy(strategy)
                    .status(SapSyncStatus.SUCCESS)
                    .received(counters.getReceived())
                    .inserted(counters.getInserted())
                    .updated(counters.getUpdated())
                    .deactivated(counters.getDeactivated())
                    .deleted(counters.getDeleted())
                    .message("Zasilenie tabeli oddzial zakończone pomyślnie")
                    .startedAt(startedAt)
                    .finishedAt(Instant.now())
                    .build());

            log.info(
                    "SAP sync SUCCESS runId={} received={} inserted={} updated={} deactivated={} deleted={}",
                    runLog.getId(),
                    counters.getReceived(),
                    counters.getInserted(),
                    counters.getUpdated(),
                    counters.getDeactivated(),
                    counters.getDeleted());
            return toDto(runLog);
        } catch (Exception ex) {
            log.error("SAP sync FAILED (dataset={}, strategy={}): {}", DATASET_ODDZIAL, strategy, ex.getMessage(), ex);
            SapSyncRunLog runLog = auditService.save(SapSyncRunLog.builder()
                    .dataset(DATASET_ODDZIAL)
                    .strategy(strategy)
                    .status(SapSyncStatus.FAILED)
                    .received(0)
                    .inserted(0)
                    .updated(0)
                    .deactivated(0)
                    .deleted(0)
                    .message(trimMessage(ex))
                    .startedAt(startedAt)
                    .finishedAt(Instant.now())
                    .build());
            throw new SapSyncException("Zasilenie SAP nie powiodło się: " + ex.getMessage(), toDto(runLog), ex);
        }
    }

    public List<SapSyncResultDto> recentRuns() {
        return auditService.recentRuns().stream().map(this::toDto).toList();
    }

    private SapSyncResultDto toDto(SapSyncRunLog runLog) {
        SapSyncResultDto dto = new SapSyncResultDto();
        dto.setRunId(runLog.getId());
        dto.setStatus(runLog.getStatus());
        dto.setStrategy(runLog.getStrategy());
        dto.setReceived(runLog.getReceived());
        dto.setInserted(runLog.getInserted());
        dto.setUpdated(runLog.getUpdated());
        dto.setDeactivated(runLog.getDeactivated());
        dto.setDeleted(runLog.getDeleted());
        dto.setMessage(runLog.getMessage());
        dto.setStartedAt(runLog.getStartedAt());
        dto.setFinishedAt(runLog.getFinishedAt());
        return dto;
    }

    private static String trimMessage(Exception ex) {
        String msg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        return msg.length() > 2000 ? msg.substring(0, 2000) : msg;
    }
}
