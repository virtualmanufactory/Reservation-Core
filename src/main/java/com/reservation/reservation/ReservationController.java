package com.reservation.reservation;

import com.reservation.dto.CreateReservationFormDTO;
import com.reservation.dto.ReservationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDTO createReservation(@Valid @RequestBody CreateReservationFormDTO dto) {
        return reservationService.createReservation(dto);
    }

    @GetMapping("/{reservationNumber}")
    public ReservationResponseDTO getByNumber(@PathVariable String reservationNumber) {
        return reservationService.getByNumber(reservationNumber);
    }

    @GetMapping
    public List<ReservationResponseDTO> listByPlace(@RequestParam Integer placeId) {
        return reservationService.listActiveForPlace(placeId);
    }
}
