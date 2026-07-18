package com.reservation.email;

import com.reservation.confirmation.Confirmation;
import com.reservation.i18n.MessageService;
import com.reservation.reservation.Reservation;
import com.reservation.util.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;
    private final MessageService messageService;

    public EmailService(JavaMailSender mailSender,
                        AppProperties appProperties,
                        MessageService messageService) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
        this.messageService = messageService;
    }

    public void sendReservationConfirmation(Reservation reservation, Confirmation confirmation) {
        String locale = reservation.getLocale();
        String code = confirmation.getConfirmationCode();
        String confirmLink = appProperties.getBaseUrl() + "/reservations/confirm?code=" + code;
        String cancelLink = appProperties.getBaseUrl() + "/restaurant/?cancel=" + code;

        String subject = messageService.get("email.confirmation.subject", locale, code);
        String body = messageService.get(
                "email.confirmation.body",
                locale,
                reservation.getOrderer().getName(),
                code,
                reservation.getDate(),
                reservation.getStartTime(),
                reservation.getPeopleCount(),
                reservation.getTable().getNumber(),
                confirmLink,
                cancelLink
        );

        send(reservation.getOrderer().getEmail(), subject, body);
    }

    public void sendCancellationNotice(Reservation reservation, Confirmation confirmation) {
        String locale = reservation.getLocale();
        String code = confirmation.getConfirmationCode();
        String subject = messageService.get("email.cancellation.subject", locale, code);
        String body = messageService.get(
                "email.cancellation.body",
                locale,
                reservation.getOrderer().getName(),
                code,
                reservation.getDate(),
                reservation.getStartTime()
        );
        send(reservation.getOrderer().getEmail(), subject, body);
    }

    public void sendTestEmail(String to, String subject, String body) {
        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException ex) {
            // Nie blokujemy rezerwacji gdy SMTP jest niewłaściwie skonfigurowany (np. placeholdery).
            log.warn("Nie udało się wysłać maila do {}: {}", to, ex.getMessage());
        }
    }
}
