package com.reservation.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Formularz rezerwacji — walidator pól wymaganych.
 */
public record CreateReservationFormDTO(
        @NotBlank(message = "Imię jest wymagane")
        @Size(max = 80)
        String ordererName,

        @NotNull(message = "Identyfikator lokalu jest wymagany")
        Integer placeId,

        @NotBlank(message = "Nazwisko jest wymagane")
        @Size(max = 80)
        String ordererSurname,

        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Nieprawidłowy email")
        String email,

        @NotBlank(message = "Numer telefonu jest wymagany")
        @Size(min = 5, max = 30)
        String phoneNumber,

        @NotNull(message = "Data jest wymagana")
        @FutureOrPresent(message = "Data nie może być z przeszłości")
        LocalDate date,

        @NotNull(message = "Godzina rozpoczęcia jest wymagana")
        LocalTime startTime,

        @NotNull(message = "Czas trwania jest wymagany")
        @Min(value = 30, message = "Minimalny czas trwania to 30 minut")
        @Max(value = 480, message = "Maksymalny czas trwania to 480 minut")
        Integer durationMinutes,

        @NotNull(message = "Liczba osób jest wymagana")
        @Min(value = 1, message = "Minimalna liczba osób to 1")
        @Max(value = 50, message = "Maksymalna liczba osób to 50")
        Integer peopleCount,

        /** Język obcy dla generatora potwierdzeń / maili: pl lub en */
        @Pattern(regexp = "pl|en|PL|EN", message = "Obsługiwane języki: pl, en")
        String locale
) {
    public String normalizedLocale() {
        if (locale == null || locale.isBlank()) {
            return "pl";
        }
        return locale.toLowerCase();
    }
}
