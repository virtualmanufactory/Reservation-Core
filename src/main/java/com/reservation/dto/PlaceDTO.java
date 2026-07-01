package com.reservation.dto;

public record PlaceDTO(
        Integer id,
        String name,
        String city,
        String street,
        Integer streetNumber,
        String postalCode,
        String postOffice,
        Integer daysCount,
        Integer tablesCount) {
}
