package com.reservation.cancellation;

import com.reservation.dto.CancelReservationDTO;
import com.reservation.dto.MessageResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class CancellationController {

    private final CancellationService cancellationService;

    public CancellationController(CancellationService cancellationService) {
        this.cancellationService = cancellationService;
    }

    @PostMapping("/cancel")
    public MessageResponseDTO cancel(@Valid @RequestBody CancelReservationDTO dto) {
        return cancellationService.cancel(dto);
    }

    @DeleteMapping("/{reservationNumber}")
    public MessageResponseDTO cancelByPath(@PathVariable String reservationNumber,
                                           @RequestParam(required = false) String reason,
                                           @RequestParam(required = false, defaultValue = "pl") String locale) {
        return cancellationService.cancel(new CancelReservationDTO(reservationNumber, reason, locale));
    }
}
