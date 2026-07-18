package com.reservation.cancellation;

import com.reservation.analytics.AnalyticsService;
import com.reservation.confirmation.Confirmation;
import com.reservation.confirmation.ConfirmationService;
import com.reservation.dto.CancelReservationDTO;
import com.reservation.dto.MessageResponseDTO;
import com.reservation.email.EmailService;
import com.reservation.i18n.MessageService;
import com.reservation.reservation.Reservation;
import com.reservation.reservation.ReservationRepository;
import com.reservation.reservation.ReservationStatus;
import com.reservation.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancellationService {

    private final ConfirmationService confirmationService;
    private final CancellationRepository cancellationRepository;
    private final ReservationRepository reservationRepository;
    private final ScheduleService scheduleService;
    private final AnalyticsService analyticsService;
    private final EmailService emailService;
    private final MessageService messageService;

    @Transactional
    public MessageResponseDTO cancel(CancelReservationDTO dto) {
        Confirmation confirmation = confirmationService.requireByCode(dto.reservationNumber());
        Reservation reservation = confirmation.getReservation();
        String locale = dto.locale() != null && !dto.locale().isBlank()
                ? dto.locale().toLowerCase()
                : reservation.getLocale();

        if (Boolean.TRUE.equals(confirmation.getCancelled())
                || reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException(messageService.get("api.error.already.cancelled", locale));
        }

        confirmation.setCancelled(true);
        confirmation.setConfirmed(false);

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        Cancellation cancellation = Cancellation.builder()
                .confirmation(confirmation)
                .confirmed(true)
                .reason(dto.reason())
                .build();
        cancellationRepository.save(cancellation);

        // Po odwołaniu — zwiększ dostępność w harmonogramie
        scheduleService.releaseTable(
                reservation.getTable().getPlace(),
                reservation.getDate(),
                reservation.getStartTime()
        );

        analyticsService.recordCancellation(
                reservation.getTable().getPlace(),
                reservation.getDate(),
                reservation.getStartTime(),
                reservation.getPeopleCount()
        );

        emailService.sendCancellationNotice(reservation, confirmation);

        return new MessageResponseDTO(messageService.get("api.cancel.success", locale));
    }
}
