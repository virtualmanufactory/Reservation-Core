package com.reservation.confirmation;

import com.reservation.reservation.Reservation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ConfirmationRepository extends CrudRepository<Confirmation, Long> {
    Optional<Confirmation> findByConfirmationCode(String confirmationCode);
    Optional<Confirmation> findByReservation(Reservation reservation);
}
