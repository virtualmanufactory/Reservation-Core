package com.reservation.reservation;

import com.reservation.dto.CreateReservationFormDTO;
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

    public ReservationService(ReservationRepository reservationRepository,
                              OrdererRepository ordererRepository,
                              TableRepository tableRepository) {
        this.reservationRepository = reservationRepository;
        this.ordererRepository = ordererRepository;
        this.tableRepository = tableRepository;
    }

    @Transactional
    public Reservation createReservation(CreateReservationFormDTO dto) {
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

        return reservationRepository.save(reservation);
    }
}
