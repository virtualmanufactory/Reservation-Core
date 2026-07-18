package com.reservation.confirmation;

import com.reservation.i18n.MessageService;
import com.reservation.orderer.Orderer;
import com.reservation.reservation.Reservation;
import com.reservation.reservation.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmationServiceTest {

    @Mock private ConfirmationRepository confirmationRepository;
    @Mock private ReservationCodeGenerator codeGenerator;
    @Mock private MessageService messageService;

    @InjectMocks
    private ConfirmationService confirmationService;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = Reservation.builder()
                .id(1)
                .orderer(Orderer.builder().name("Anna").email("a@b.c").build())
                .date(LocalDate.of(2026, 7, 20))
                .status(ReservationStatus.ACTIVE)
                .locale("pl")
                .build();
    }

    @Test
    void createsConfirmationWithGeneratedCode() {
        when(confirmationRepository.findByReservation(reservation)).thenReturn(Optional.empty());
        when(codeGenerator.generate(reservation.getDate())).thenReturn("RES-20260720-ABCDEF");
        when(confirmationRepository.findByConfirmationCode("RES-20260720-ABCDEF")).thenReturn(Optional.empty());
        when(confirmationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Confirmation confirmation = confirmationService.getOrCreateForReservation(reservation);

        assertThat(confirmation.getConfirmationCode()).isEqualTo("RES-20260720-ABCDEF");
        assertThat(confirmation.getConfirmed()).isFalse();
        assertThat(confirmation.getCancelled()).isFalse();
    }

    @Test
    void confirmMarksAsConfirmed() {
        Confirmation confirmation = Confirmation.builder()
                .confirmationCode("RES-1")
                .reservation(reservation)
                .confirmed(false)
                .cancelled(false)
                .build();
        when(confirmationRepository.findByConfirmationCode("RES-1")).thenReturn(Optional.of(confirmation));
        when(messageService.get("api.confirm.success", "pl")).thenReturn("ok");
        when(confirmationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String msg = confirmationService.confirm("RES-1");

        assertThat(msg).isEqualTo("ok");
        assertThat(confirmation.getConfirmed()).isTrue();
    }

    @Test
    void confirmRejectsCancelled() {
        Confirmation confirmation = Confirmation.builder()
                .confirmationCode("RES-1")
                .reservation(reservation)
                .confirmed(false)
                .cancelled(true)
                .build();
        when(confirmationRepository.findByConfirmationCode("RES-1")).thenReturn(Optional.of(confirmation));
        when(messageService.get("api.error.already.cancelled", "pl")).thenReturn("cancelled");

        assertThatThrownBy(() -> confirmationService.confirm("RES-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("cancelled");
    }
}
