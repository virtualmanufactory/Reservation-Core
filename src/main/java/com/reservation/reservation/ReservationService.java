package com.reservation.reservation;

import com.reservation.analytics.AnalyticsService;
import com.reservation.confirmation.Confirmation;
import com.reservation.confirmation.ConfirmationRepository;
import com.reservation.confirmation.ConfirmationService;
import com.reservation.dto.CreateReservationFormDTO;
import com.reservation.dto.ReservationResponseDTO;
import com.reservation.email.EmailService;
import com.reservation.i18n.MessageService;
import com.reservation.orderer.Orderer;
import com.reservation.orderer.OrdererRepository;
import com.reservation.place.Place;
import com.reservation.place.PlaceRepository;
import com.reservation.schedule.ScheduleService;
import com.reservation.schedule.ScheduleSlot;
import com.reservation.table.TableEntity;
import com.reservation.table.TableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final OrdererRepository ordererRepository;
    private final TableRepository tableRepository;
    private final PlaceRepository placeRepository;
    private final EmailService emailService;
    private final ConfirmationService confirmationService;
    private final ConfirmationRepository confirmationRepository;
    private final ScheduleService scheduleService;
    private final AnalyticsService analyticsService;
    private final MessageService messageService;

    public ReservationService(ReservationRepository reservationRepository,
                              OrdererRepository ordererRepository,
                              TableRepository tableRepository,
                              PlaceRepository placeRepository,
                              EmailService emailService,
                              ConfirmationService confirmationService,
                              ConfirmationRepository confirmationRepository,
                              ScheduleService scheduleService,
                              AnalyticsService analyticsService,
                              MessageService messageService) {
        this.reservationRepository = reservationRepository;
        this.ordererRepository = ordererRepository;
        this.tableRepository = tableRepository;
        this.placeRepository = placeRepository;
        this.emailService = emailService;
        this.confirmationService = confirmationService;
        this.confirmationRepository = confirmationRepository;
        this.scheduleService = scheduleService;
        this.analyticsService = analyticsService;
        this.messageService = messageService;
    }

    @Transactional
    public ReservationResponseDTO createReservation(CreateReservationFormDTO dto) {
        String locale = dto.normalizedLocale();

        Place place = placeRepository.findById(dto.placeId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono lokalu"));

        if (!place.isInitialized()) {
            throw new IllegalStateException("Lokal nie został jeszcze skonfigurowany");
        }

        ScheduleSlot slot = scheduleService.requireOpenSlot(place, dto.date(), dto.startTime());

        Orderer orderer = ordererRepository.findByEmail(dto.email())
                .orElseGet(() -> {
                    Orderer newOrderer = new Orderer();
                    newOrderer.setName(dto.ordererName());
                    newOrderer.setSurname(dto.ordererSurname());
                    newOrderer.setEmail(dto.email());
                    newOrderer.setPhoneNumber(dto.phoneNumber());
                    return ordererRepository.save(newOrderer);
                });

        LocalTime endTime = dto.startTime().plusMinutes(dto.durationMinutes());

        TableEntity table = tableRepository.findFirstAvailableTable(
                dto.placeId(),
                dto.date(),
                dto.startTime(),
                endTime,
                dto.peopleCount()
        ).orElseThrow(() -> new IllegalStateException(
                messageService.get("api.error.no.table", locale)));

        Reservation reservation = new Reservation();
        reservation.setOrderer(orderer);
        reservation.setTable(table);
        reservation.setDate(dto.date());
        reservation.setStartTime(dto.startTime());
        reservation.setEndTime(endTime);
        reservation.setDurationMinutes(dto.durationMinutes());
        reservation.setPeopleCount(dto.peopleCount());
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setLocale(locale);

        Reservation savedReservation = reservationRepository.save(reservation);

        scheduleService.reserveTable(slot);
        analyticsService.recordReservation(place, dto.date(), dto.startTime(), dto.peopleCount());

        Confirmation confirmation = confirmationService.getOrCreateForReservation(savedReservation);
        emailService.sendReservationConfirmation(savedReservation, confirmation);

        return mapToResponseDTO(savedReservation, confirmation);
    }

    @Transactional(readOnly = true)
    public ReservationResponseDTO getByNumber(String reservationNumber) {
        Confirmation confirmation = confirmationService.requireByCode(reservationNumber);
        return mapToResponseDTO(confirmation.getReservation(), confirmation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> listActiveForPlace(Integer placeId) {
        List<ReservationResponseDTO> result = new ArrayList<>();
        for (Reservation reservation : reservationRepository.findAll()) {
            if (reservation.getTable() == null
                    || reservation.getTable().getPlace() == null
                    || !placeId.equals(reservation.getTable().getPlace().getId())
                    || reservation.getStatus() != ReservationStatus.ACTIVE) {
                continue;
            }
            confirmationRepository.findByReservation(reservation)
                    .ifPresent(c -> result.add(mapToResponseDTO(reservation, c)));
        }
        return result;
    }

    private ReservationResponseDTO mapToResponseDTO(Reservation reservation, Confirmation confirmation) {
        return new ReservationResponseDTO(
                reservation.getId(),
                confirmation.getConfirmationCode(),
                reservation.getDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPeopleCount(),
                reservation.getTable().getNumber(),
                reservation.getOrderer().getEmail(),
                reservation.getStatus().name(),
                reservation.getLocale(),
                Boolean.TRUE.equals(confirmation.getConfirmed())
        );
    }
}
