package com.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TableSetupDto(
        @NotNull @Min(1) Integer number,
        @NotNull @Min(1) Integer seatsCount,
        boolean active
) {
}
