package com.reservation.reservation;

import com.reservation.dto.CreateReservationFormDTO;
import com.reservation.dto.ReservationResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDTO createReservation(@RequestBody CreateReservationFormDTO dto) {
        return reservationService.createReservation(dto);
    }
}