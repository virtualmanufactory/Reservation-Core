package com.reservation.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateReservationFormDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void acceptsValidForm() {
        CreateReservationFormDTO dto = validForm();
        assertThat(validator.validate(dto)).isEmpty();
        assertThat(dto.normalizedLocale()).isEqualTo("en");
    }

    @Test
    void rejectsMissingRequiredFields() {
        CreateReservationFormDTO dto = new CreateReservationFormDTO(
                "", null, null, "not-an-email", "", null, null, 10, 0, "de"
        );

        Set<ConstraintViolation<CreateReservationFormDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString()))
                .contains("ordererName", "placeId", "ordererSurname", "email", "phoneNumber",
                        "date", "startTime", "durationMinutes", "peopleCount", "locale");
    }

    @Test
    void defaultsLocaleToPlWhenNull() {
        CreateReservationFormDTO dto = new CreateReservationFormDTO(
                "Anna", 1, "Nowak", "anna@example.com", "500600700",
                LocalDate.now().plusDays(1), LocalTime.of(18, 0), 90, 2, null
        );

        assertThat(validator.validate(dto)).isEmpty();
        assertThat(dto.normalizedLocale()).isEqualTo("pl");
    }

    private CreateReservationFormDTO validForm() {
        return new CreateReservationFormDTO(
                "Anna", 1, "Nowak", "anna@example.com", "500600700",
                LocalDate.now().plusDays(1), LocalTime.of(18, 0), 90, 2, "EN"
        );
    }
}
