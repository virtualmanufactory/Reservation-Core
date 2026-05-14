package com.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateReservationFormDTO(
   String ordererName,
   Integer placeId,
   String ordererSurname,
   String email,
   String phoneNumber,
   LocalDate date,
   LocalTime startTime,
   Integer durationMinutes,
   Integer peopleCount
) {

}
