package com.reservation.sap.dto;

import com.reservation.sap.model.SapSyncStatus;

import java.time.Instant;

public class SapSyncResultDto {

    private Long runId;
    private SapSyncStatus status;
    private String strategy;
    private int received;
    private int inserted;
    private int updated;
    private int deactivated;
    private int deleted;
    private String message;
    private Instant startedAt;
    private Instant finishedAt;

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public SapSyncStatus getStatus() {
        return status;
    }

    public void setStatus(SapSyncStatus status) {
        this.status = status;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public int getReceived() {
        return received;
    }

    public void setReceived(int received) {
        this.received = received;
    }

    public int getInserted() {
        return inserted;
    }

    public void setInserted(int inserted) {
        this.inserted = inserted;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public int getDeactivated() {
        return deactivated;
    }

    public void setDeactivated(int deactivated) {
        this.deactivated = deactivated;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }
}
