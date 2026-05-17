package com.reservation.email;

import com.reservation.reservation.Reservation;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendReservationConfirmation(Reservation reservation) {
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

