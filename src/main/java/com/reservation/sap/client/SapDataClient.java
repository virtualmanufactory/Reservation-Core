package com.reservation.sap.client;

import com.reservation.sap.dto.OddzialDto;

import java.util.List;

public interface SapDataClient {

    List<OddzialDto> fetchOddzialy();
}
