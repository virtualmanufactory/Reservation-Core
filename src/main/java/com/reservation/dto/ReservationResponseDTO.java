package com.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponseDTO(
        Integer id,
        String reservationNumber,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Integer peopleCount,
        Integer tableNumber,
        String ordererEmail,
        String status,
        String locale,
        boolean confirmed
) { }
