package com.reservation.sap.scheduler;

import com.reservation.sap.config.SapSyncProperties;
import com.reservation.sap.service.SapSyncException;
import com.reservation.sap.service.SapSyncOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.sap", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SapSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(SapSyncScheduler.class);

    private final SapSyncOrchestrator orchestrator;
    private final SapSyncProperties properties;

    public SapSyncScheduler(SapSyncOrchestrator orchestrator, SapSyncProperties properties) {
        this.orchestrator = orchestrator;
        this.properties = properties;
    }

    @Scheduled(cron = "${app.sap.cron:0 0 * * * *}")
    public void runScheduledSync() {
        if (!properties.isEnabled()) {
            return;
        }
        log.info("Zaplanowane zasilenie SAP (oddzial) uruchomione");
        try {
            orchestrator.syncOddzialy();
        } catch (SapSyncException ex) {
            log.warn("Zaplanowane zasilenie SAP zakończone błędem (runId={})",
                    ex.getResult() != null ? ex.getResult().getRunId() : null);
        }
    }
}
