package com.reservation.email;

import com.reservation.dto.TestEmailRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test")
    public ResponseEntity<String> sendTestEmail(@RequestBody TestEmailRequestDTO dto) {
        emailService.sendTestEmail(dto.to(), dto.subject(), dto.body());
        return ResponseEntity.ok("Test email sent");
    }
}
