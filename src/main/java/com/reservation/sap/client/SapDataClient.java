package com.reservation.sap.client;

import com.reservation.sap.dto.BranchDto;

import java.util.List;

public interface SapDataClient {

    List<BranchDto> fetchBranches();
}
