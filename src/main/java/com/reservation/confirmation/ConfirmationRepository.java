package com.reservation.confirmation;

import com.reservation.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmationRepository extends JpaRepository<Confirmation, Integer> {
    Optional<Confirmation> findByConfirmationCode(String confirmationCode);
    Optional<Confirmation> findByReservation(Reservation reservation);
}
