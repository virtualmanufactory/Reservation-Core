package com.reservation.sap.service;

import com.reservation.sap.model.SapSyncRunLog;
import com.reservation.sap.repository.SapSyncRunLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SapSyncAuditService {

    private final SapSyncRunLogRepository runLogRepository;

    public SapSyncAuditService(SapSyncRunLogRepository runLogRepository) {
        this.runLogRepository = runLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SapSyncRunLog save(SapSyncRunLog runLog) {
        return runLogRepository.save(runLog);
    }

    @Transactional(readOnly = true)
    public List<SapSyncRunLog> recentRuns() {
        return runLogRepository.findTop20ByOrderByStartedAtDesc();
    }
}
