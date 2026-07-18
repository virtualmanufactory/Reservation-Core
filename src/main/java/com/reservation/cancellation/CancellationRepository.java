package com.reservation.cancellation;

import com.reservation.confirmation.Confirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface CancellationRepository extends JpaRepository<Cancellation, Integer> {

    Optional<Cancellation> findByConfirmation(Confirmation confirmation);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Cancellation c WHERE c.cancelledAt < :cutoff")
    int deleteByCancelledAtBefore(@Param("cutoff") Instant cutoff);
}
