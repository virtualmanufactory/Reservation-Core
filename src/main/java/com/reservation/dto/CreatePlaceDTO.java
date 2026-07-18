package com.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePlaceDTO(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Size(max = 120) String street,
        @NotNull Integer streetNumber,
        @NotBlank @Size(max = 20) String postalCode,
        @NotBlank @Size(max = 80) String postOffice
) { }
