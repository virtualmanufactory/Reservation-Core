package com.reservation.email;

import com.reservation.dto.TestEmailRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//testowy endpoint do testowania wysyłki emaili
@RestController
@RequestMapping("/api/email")
public class EmailController {
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test")
    public ResponseEntity<String> sendTestEmail(@RequestParam TestEmailRequestDTO dto) {
        emailService.sendTestEmail(dto.to(), dto.subject(), dto.body());
        return ResponseEntity.ok("Test email sent");
    }
}
