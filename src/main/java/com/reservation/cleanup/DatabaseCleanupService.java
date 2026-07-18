package com.reservation.cleanup;

import com.reservation.analytics.AnalyticsService;
import com.reservation.analytics.ReservationAnalyticsRepository;
import com.reservation.cancellation.CancellationRepository;
import com.reservation.confirmation.ConfirmationRepository;
import com.reservation.reservation.Reservation;
import com.reservation.reservation.ReservationRepository;
import com.reservation.reservation.ReservationStatus;
import com.reservation.schedule.ScheduleService;
import com.reservation.schedule.ScheduleSlotRepository;
import com.reservation.util.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Mechanizmy czyszczenia bazy:
 * 1) wygasanie niepotwierdzonych rezerwacji (po N godzinach) + przywrócenie dostępności,
 * 2) usuwanie starych slotów harmonogramu (po N dniach),
 * 3) usuwanie starych rekordów analityki i odwołań.
 */
@Service
public class DatabaseCleanupService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseCleanupService.class);

    private final AppProperties appProperties;
    private final ReservationRepository reservationRepository;
    private final ConfirmationRepository confirmationRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;
    private final ReservationAnalyticsRepository analyticsRepository;
    private final CancellationRepository cancellationRepository;
    private final ScheduleService scheduleService;
    private final AnalyticsService analyticsService;

    public DatabaseCleanupService(AppProperties appProperties,
                                  ReservationRepository reservationRepository,
                                  ConfirmationRepository confirmationRepository,
                                  ScheduleSlotRepository scheduleSlotRepository,
                                  ReservationAnalyticsRepository analyticsRepository,
                                  CancellationRepository cancellationRepository,
                                  ScheduleService scheduleService,
                                  AnalyticsService analyticsService) {
        this.appProperties = appProperties;
        this.reservationRepository = reservationRepository;
        this.confirmationRepository = confirmationRepository;
        this.scheduleSlotRepository = scheduleSlotRepository;
        this.analyticsRepository = analyticsRepository;
        this.cancellationRepository = cancellationRepository;
        this.scheduleService = scheduleService;
        this.analyticsService = analyticsService;
    }

    /** Co godzinę: niepotwierdzone rezerwacje starsze niż próg → EXPIRED + zwolnienie stolika. */
    @Scheduled(cron = "0 15 * * * *")
    @Transactional
    public void expireUnconfirmedReservations() {
        if (!appProperties.getCleanup().isEnabled()) {
            return;
        }
        Instant cutoff = Instant.now().minus(
                appProperties.getCleanup().getUnconfirmedHours(), ChronoUnit.HOURS);
        List<Reservation> stale = reservationRepository.findUnconfirmedOlderThan(cutoff);
        int count = 0;
        for (Reservation reservation : stale) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            confirmationRepository.findByReservation(reservation).ifPresent(c -> {
                c.setCancelled(true);
                confirmationRepository.save(c);
            });
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
            reservationRepository.save(reservation);
            count++;
        }
        if (count > 0) {
            log.info("Cleanup: wygaszono {} niepotwierdzonych rezerwacji", count);
        }
    }

    /** Codziennie o 03:30: stare sloty, analityka i odwołania. */
    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void purgeHistoricalData() {
        if (!appProperties.getCleanup().isEnabled()) {
            return;
        }
        LocalDate slotCutoff = LocalDate.now().minusDays(appProperties.getCleanup().getPastSlotsDays());
        Instant cancelCutoff = Instant.now().minus(
                appProperties.getCleanup().getPastSlotsDays(), ChronoUnit.DAYS);

        int slots = scheduleSlotRepository.deleteByDateBefore(slotCutoff);
        int analytics = analyticsRepository.deleteByDateBefore(slotCutoff);
        int cancellations = cancellationRepository.deleteByCancelledAtBefore(cancelCutoff);

        log.info("Cleanup: usunięto sloty={}, analityka={}, odwołania={}", slots, analytics, cancellations);
    }
}
