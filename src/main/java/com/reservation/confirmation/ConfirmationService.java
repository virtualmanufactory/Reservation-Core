package com.reservation.confirmation;

import com.reservation.reservation.Reservation;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ConfirmationService {

    private final ConfirmationRepository confirmationRepository;

    public ConfirmationService(ConfirmationRepository confirmationRepository) {
        this.confirmationRepository = confirmationRepository;
    }

    public Confirmation getOrCreateForReservation(Reservation reservation) {
        return confirmationRepository.findByReservation(reservation)
                .orElseGet(() -> {
                    Confirmation confirmation = new Confirmation();
                    confirmation.setReservation(reservation);
                    confirmation.setConfirmationCode(UUID.randomUUID().toString());
                    confirmation.setConfirmed(false);
                    confirmation.setCancelled(false);
                    return confirmationRepository.save(confirmation);
                });
    }

    public void confirm(String code) {
        Confirmation confirmation = confirmationRepository.findByConfirmationCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono potwierdzenia"));

        if (Boolean.TRUE.equals(confirmation.getCancelled())) {
            throw new IllegalStateException("Rezerwacja została anulowana");
        }

        confirmation.setConfirmed(true);
        confirmationRepository.save(confirmation);
    }
}
