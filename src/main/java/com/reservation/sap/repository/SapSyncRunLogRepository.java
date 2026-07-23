package com.reservation.sap.repository;

import com.reservation.sap.model.SapSyncRunLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SapSyncRunLogRepository extends JpaRepository<SapSyncRunLog, Long> {

    List<SapSyncRunLog> findTop20ByOrderByStartedAtDesc();
}
