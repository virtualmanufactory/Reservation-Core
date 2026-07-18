package com.reservation.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record AnalyticsDTO(
        LocalDate date,
        LocalTime hour,
        Integer reservedTables,
        Integer cancelledTables,
        Integer peopleCount,
        Instant updatedAt
) { }
