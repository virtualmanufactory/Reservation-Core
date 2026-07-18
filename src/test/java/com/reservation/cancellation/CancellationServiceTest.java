package com.reservation.cancellation;

import com.reservation.analytics.AnalyticsService;
import com.reservation.confirmation.Confirmation;
import com.reservation.confirmation.ConfirmationService;
import com.reservation.dto.CancelReservationDTO;
import com.reservation.dto.MessageResponseDTO;
import com.reservation.email.EmailService;
import com.reservation.i18n.MessageService;
import com.reservation.place.Place;
import com.reservation.reservation.Reservation;
import com.reservation.reservation.ReservationRepository;
import com.reservation.reservation.ReservationStatus;
import com.reservation.schedule.ScheduleService;
import com.reservation.table.TableEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancellationServiceTest {

    @Mock private ConfirmationService confirmationService;
    @Mock private CancellationRepository cancellationRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private ScheduleService scheduleService;
    @Mock private AnalyticsService analyticsService;
    @Mock private EmailService emailService;
    @Mock private MessageService messageService;

    @InjectMocks
    private CancellationService cancellationService;

    private Confirmation confirmation;
    private Reservation reservation;
    private Place place;

    @BeforeEach
    void setUp() {
        place = Place.builder().id(1).name("Bistro").build();
        TableEntity table = TableEntity.builder().id(5).number(2).place(place).build();

        reservation = Reservation.builder()
                .id(9)
                .table(table)
                .date(LocalDate.of(2026, 8, 1))
                .startTime(LocalTime.of(13, 0))
                .peopleCount(2)
                .status(ReservationStatus.ACTIVE)
                .locale("pl")
                .build();

        confirmation = Confirmation.builder()
                .id(3)
                .reservation(reservation)
                .confirmationCode("RES-20260801-ABC123")
                .confirmed(true)
                .cancelled(false)
                .build();
    }

    @Test
    void cancelRestoresAvailabilityAndRecordsAnalytics() {
        when(confirmationService.requireByCode("RES-20260801-ABC123")).thenReturn(confirmation);
        when(messageService.get(eq("api.cancel.success"), eq("pl"))).thenReturn("odwołano");
        when(cancellationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MessageResponseDTO response = cancellationService.cancel(
                new CancelReservationDTO("RES-20260801-ABC123", "test", "pl"));

        assertThat(response.message()).isEqualTo("odwołano");
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(confirmation.getCancelled()).isTrue();
        verify(scheduleService).releaseTable(place, reservation.getDate(), reservation.getStartTime());
        verify(analyticsService).recordCancellation(place, reservation.getDate(), reservation.getStartTime(), 2);
        verify(emailService).sendCancellationNotice(reservation, confirmation);
        verify(cancellationRepository).save(any(Cancellation.class));
    }

    @Test
    void cancelRejectsAlreadyCancelledReservation() {
        confirmation.setCancelled(true);
        when(confirmationService.requireByCode(anyString())).thenReturn(confirmation);
        when(messageService.get(eq("api.error.already.cancelled"), anyString()))
                .thenReturn("już odwołana");

        assertThatThrownBy(() -> cancellationService.cancel(
                new CancelReservationDTO("RES-20260801-ABC123", null, "pl")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("już odwołana");

        verify(scheduleService, never()).releaseTable(any(), any(), any());
    }
}
