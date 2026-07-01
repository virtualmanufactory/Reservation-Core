package com.reservation.dto;

public record TableSetupDto(
        Integer number,
        Integer seatsCount,
        boolean active
) {
}
