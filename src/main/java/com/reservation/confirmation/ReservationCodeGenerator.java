package com.reservation.confirmation;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generator numerów potwierdzeń rezerwacji.
 * Format: RES-YYYYMMDD-XXXXXX
 */
@Component
public class ReservationCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;
    private final SecureRandom random = new SecureRandom();

    public String generate(LocalDate date) {
        String dayPart = (date != null ? date : LocalDate.now()).format(DAY);
        return "RES-" + dayPart + "-" + randomSuffix(6);
    }

    private String randomSuffix(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
