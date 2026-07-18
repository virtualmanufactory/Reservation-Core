package com.reservation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

public record PlaceSetupRequestDto(
        @NotNull @Min(2024) @Max(2100) Integer year,
        boolean open,
        LocalTime defaultOpenFrom,
        LocalTime defaultOpenTo,
        @Valid List<TableSetupDto> tables
) {
}
