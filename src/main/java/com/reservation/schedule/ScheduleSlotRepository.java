package com.reservation.schedule;

import com.reservation.place.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Integer> {

    Optional<ScheduleSlot> findByPlaceAndDateAndHour(Place place, LocalDate date, LocalTime hour);

    List<ScheduleSlot> findByPlaceIdAndDateOrderByHourAsc(Integer placeId, LocalDate date);

    List<ScheduleSlot> findByPlaceIdAndDateBetweenOrderByDateAscHourAsc(
            Integer placeId, LocalDate from, LocalDate to);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ScheduleSlot s WHERE s.date < :cutoff")
    int deleteByDateBefore(@Param("cutoff") LocalDate cutoff);
}
