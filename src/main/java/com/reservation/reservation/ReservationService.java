package com.reservation.reservation;

import com.reservation.confirmation.Confirmation;
import com.reservation.confirmation.ConfirmationService;
import com.reservation.dto.CreateReservationFormDTO;
import com.reservation.dto.ReservationResponseDTO;
import com.reservation.email.EmailService;
import com.reservation.orderer.Orderer;
import com.reservation.orderer.OrdererRepository;
import com.reservation.table.TableEntity;
import com.reservation.table.TableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final OrdererRepository ordererRepository;
    private final TableRepository tableRepository;
    private final EmailService emailService;
    private final ConfirmationService confirmationService;
    public ReservationService(ReservationRepository reservationRepository,
                              OrdererRepository ordererRepository,
                              TableRepository tableRepository,
                              EmailService emailService,
                              ConfirmationService confirmationService
                              ) {
        this.reservationRepository = reservationRepository;
        this.ordererRepository = ordererRepository;
        this.tableRepository = tableRepository;
        this.emailService = emailService;
        this.confirmationService = confirmationService;
    }

    @Transactional
    public ReservationResponseDTO createReservation(CreateReservationFormDTO dto) {
        Orderer orderer = (Orderer) ordererRepository.findByEmail(dto.email())
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
        ).orElseThrow(() -> new IllegalStateException("Brak wolnego stolika na wybraną datę i godzinę"));

        Reservation reservation = new Reservation();
        reservation.setOrderer(orderer);
        reservation.setTable(table);
        reservation.setDate(dto.date());
        reservation.setStartTime(dto.startTime());
        reservation.setEndTime(endTime);
        reservation.setDurationMinutes(dto.durationMinutes());
        reservation.setPeopleCount(dto.peopleCount());

        Reservation savedReservation = reservationRepository.save(reservation);
        Confirmation confirmation = confirmationService.getOrCreateForReservation(savedReservation);
        emailService.sendReservationConfirmation(savedReservation, confirmation);

        return mapToResponseDTO(savedReservation);
    }

    private ReservationResponseDTO mapToResponseDTO(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getId(),
                reservation.getDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPeopleCount(),
                reservation.getTable().getNumber(),
                reservation.getOrderer().getEmail()
        );
    }

}
