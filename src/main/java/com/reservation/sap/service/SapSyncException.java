package com.reservation.sap.service;

import com.reservation.sap.dto.SapSyncResultDto;

public class SapSyncException extends RuntimeException {

    private final SapSyncResultDto result;

    public SapSyncException(String message, SapSyncResultDto result, Throwable cause) {
        super(message, cause);
        this.result = result;
    }

    public SapSyncResultDto getResult() {
        return result;
    }
}
