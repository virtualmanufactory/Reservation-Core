package com.reservation.analytics;

import com.reservation.place.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationAnalyticsRepository extends JpaRepository<ReservationAnalytics, Integer> {

    Optional<ReservationAnalytics> findByPlaceAndDateAndHour(Place place, LocalDate date, LocalTime hour);

    List<ReservationAnalytics> findByPlaceIdAndDateBetweenOrderByDateAscHourAsc(
            Integer placeId, LocalDate from, LocalDate to);

    List<ReservationAnalytics> findByPlaceIdAndDateOrderByHourAsc(Integer placeId, LocalDate date);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ReservationAnalytics a WHERE a.date < :cutoff")
    int deleteByDateBefore(@Param("cutoff") LocalDate cutoff);
}
