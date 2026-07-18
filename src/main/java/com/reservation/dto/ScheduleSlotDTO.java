package com.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleSlotDTO(
        Integer id,
        LocalDate date,
        LocalTime hour,
        Integer totalTables,
        Integer availableTables,
        Integer reservedTables,
        boolean locked
) { }
