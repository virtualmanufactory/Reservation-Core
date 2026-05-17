package com.reservation.dto;

public record CreateUserDTO(
    String ordererName,
    String ordererSurname,
    String email,
    String phoneNumber,
    String login,
    String password
) { }
