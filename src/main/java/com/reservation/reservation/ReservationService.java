package com.reservation.reservation;

import com.reservation.dto.CreateReservationFormDTO;
import com.reservation.orderer.Orderer;
import com.reservation.orderer.OrdererRepository;
import com.reservation.table.TableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // Sprawdź, czy zamawiający już istnieje (np. po e-mailu)
        Orderer orderer = (Orderer) ordererRepository.findByEmail(dto.email())
                .orElseGet(() -> {
                    Orderer newOrderer = new Orderer();
                    newOrderer.setName(dto.ordererName());
                    newOrderer.setSurname(dto.ordererSurname());
                    newOrderer.setEmail(dto.email());
                    newOrderer.setPhoneNumber(dto.phoneNumber());
                    return ordererRepository.save(newOrderer);
                });

        //Znajdź odpowiedni timeTable na podstawie daty zamówienia i godziny

        // Wybierz wolny stół (tu zakładamy istnienie metody findAvailableTable)


        // Utwórz i zapisz rezerwację
        Reservation reservation = new Reservation();
        reservation.setOrderer(orderer);
        //reservation.setTable(table);
        reservation.setDate(dto.date());
        reservation.setStartTime(dto.startTime());
        reservation.setDurationMinutes(dto.durationMinutes());
        reservation.setPeopleCount(dto.peopleCount());
        //create confirmation
        return reservationRepository.save(reservation);
    }
}
