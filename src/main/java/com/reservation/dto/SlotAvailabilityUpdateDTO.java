package com.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record SlotAvailabilityUpdateDTO(
        @NotNull LocalDate date,
        @NotNull LocalTime hour,
        Boolean locked,
        @Min(0) Integer availableTables
) { }
