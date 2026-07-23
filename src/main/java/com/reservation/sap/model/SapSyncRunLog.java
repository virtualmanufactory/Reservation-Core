package com.reservation.sap.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Audit trail of SAP feed runs (success and failure).
 */
@Entity
@Table(name = "sap_sync_run_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SapSyncRunLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "dataset", nullable = false, length = 64)
    private String dataset;

    @Column(name = "strategy", nullable = false, length = 32)
    private String strategy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SapSyncStatus status;

    @Column(nullable = false)
    private int received;

    @Column(nullable = false)
    private int inserted;

    @Column(nullable = false)
    private int updated;

    @Column(nullable = false)
    private int deactivated;

    @Column(nullable = false)
    private int deleted;

    @Lob
    @Column(name = "message")
    private String message;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;
}
