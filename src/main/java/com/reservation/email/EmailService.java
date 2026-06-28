package com.reservation.email;

import com.reservation.confirmation.Confirmation;
import com.reservation.reservation.Reservation;
import com.reservation.util.AppProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public EmailService(JavaMailSender mailSender, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
    }

    public void sendReservationConfirmation(Reservation reservation, Confirmation confirmation) {
        String code = confirmation.getConfirmationCode();
        String confirmLink = appProperties.getBaseUrl()
                + "/reservations/confirm?code="
                + confirmation.getConfirmationCode();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(reservation.getOrderer().getEmail());
        message.setSubject("Potwierdzenie rezerwacji");
        message.setText(
                "Dzień dobry " + reservation.getOrderer().getName() + ",\n\n" +
                        "Twoja rezerwacja została przyjęta.\n" +
                        "Data: " + reservation.getDate() + "\n" +
                        "Godzina: " + reservation.getStartTime() + "\n" +
                        "Liczba osób: " + reservation.getPeopleCount() + "\n" +
                        "Stolik: " + reservation.getTable().getNumber() + "\n\n" +
                        "Kod potwierdzenia: " + code + "\n" +
                        "Potwierdź rezerwację klikając w link:\n" + confirmLink + "\n\n" +
                        "Dziękujemy."
        );

        mailSender.send(message);
    }
    public void sendTestEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}

