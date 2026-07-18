package com.reservation.confirmation;

import com.reservation.i18n.MessageService;
import com.reservation.reservation.Reservation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmationService {

    private final ConfirmationRepository confirmationRepository;
    private final ReservationCodeGenerator codeGenerator;
    private final MessageService messageService;

    public ConfirmationService(ConfirmationRepository confirmationRepository,
                               ReservationCodeGenerator codeGenerator,
                               MessageService messageService) {
        this.confirmationRepository = confirmationRepository;
        this.codeGenerator = codeGenerator;
        this.messageService = messageService;
    }

    @Transactional
    public Confirmation getOrCreateForReservation(Reservation reservation) {
        return confirmationRepository.findByReservation(reservation)
                .orElseGet(() -> {
                    Confirmation confirmation = new Confirmation();
                    confirmation.setReservation(reservation);
                    confirmation.setConfirmationCode(uniqueCode(reservation));
                    confirmation.setConfirmed(false);
                    confirmation.setCancelled(false);
                    return confirmationRepository.save(confirmation);
                });
    }

    @Transactional
    public String confirm(String code) {
        Confirmation confirmation = confirmationRepository.findByConfirmationCode(code)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageService.get("api.error.not.found", "pl")));

        if (Boolean.TRUE.equals(confirmation.getCancelled())) {
            throw new IllegalStateException(
                    messageService.get("api.error.already.cancelled", reservationLocale(confirmation)));
        }

        confirmation.setConfirmed(true);
        confirmationRepository.save(confirmation);
        return messageService.get("api.confirm.success", reservationLocale(confirmation));
    }

    public Confirmation requireByCode(String code) {
        return confirmationRepository.findByConfirmationCode(code)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageService.get("api.error.not.found", "pl")));
    }

    private String uniqueCode(Reservation reservation) {
        String code;
        do {
            code = codeGenerator.generate(reservation.getDate());
        } while (confirmationRepository.findByConfirmationCode(code).isPresent());
        return code;
    }

    private String reservationLocale(Confirmation confirmation) {
        if (confirmation.getReservation() != null && confirmation.getReservation().getLocale() != null) {
            return confirmation.getReservation().getLocale();
        }
        return "pl";
    }
}
