package com.reservation.dto;

import java.time.LocalTime;
import java.util.List;

public record PlaceSetupRequestDto(
        Integer year,
        boolean open,
        LocalTime defaultOpenFrom,
        LocalTime defaultOpenTo,
        List<TableSetupDto> tables
) {
}
