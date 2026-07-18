package com.reservation.confirmation;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationCodeGeneratorTest {

    private final ReservationCodeGenerator generator = new ReservationCodeGenerator();

    @Test
    void generatesCodeWithExpectedFormat() {
        String code = generator.generate(LocalDate.of(2026, 7, 20));

        assertThat(code).matches("RES-20260720-[A-Z2-9]{6}");
    }

    @Test
    void usesTodayWhenDateIsNull() {
        String code = generator.generate(null);
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);

        assertThat(code).startsWith("RES-" + today + "-");
        assertThat(code).hasSize("RES-".length() + 8 + 1 + 6);
    }

    @Test
    void generatesUniqueCodes() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            codes.add(generator.generate(LocalDate.of(2026, 1, 1)));
        }
        assertThat(codes).hasSize(100);
    }
}
