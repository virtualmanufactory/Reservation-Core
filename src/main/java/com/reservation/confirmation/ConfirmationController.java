package com.reservation.confirmation;

import com.reservation.dto.MessageResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ConfirmationController {
    private final ConfirmationService confirmationService;

    public ConfirmationController(ConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

    @GetMapping("/confirm")
    public ResponseEntity<MessageResponseDTO> confirm(@RequestParam String code) {
        String message = confirmationService.confirm(code);
        return ResponseEntity.ok(new MessageResponseDTO(message));
    }
}
