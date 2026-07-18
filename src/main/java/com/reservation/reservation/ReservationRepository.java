package com.reservation.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    @Query("""
            SELECT r FROM Reservation r
            JOIN Confirmation c ON c.reservation = r
            WHERE r.status = com.reservation.reservation.ReservationStatus.ACTIVE
              AND c.confirmed = false
              AND c.cancelled = false
              AND r.createdAt < :cutoff
            """)
    List<Reservation> findUnconfirmedOlderThan(@Param("cutoff") Instant cutoff);
}
