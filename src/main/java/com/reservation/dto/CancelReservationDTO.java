package com.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelReservationDTO(
        @NotBlank(message = "Numer rezerwacji jest wymagany")
        String reservationNumber,

        @Size(max = 500)
        String reason,

        @jakarta.validation.constraints.Pattern(regexp = "pl|en|PL|EN|", message = "Obsługiwane języki: pl, en")
        String locale
) { }
