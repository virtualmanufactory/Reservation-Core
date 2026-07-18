package com.reservation.calendar;

import com.reservation.place.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface CalendarDayRepository extends JpaRepository<CalendarDay, Integer> {

    Optional<CalendarDay> findByPlaceAndDate(Place place, LocalDate date);

    Optional<CalendarDay> findByPlaceIdAndDate(Integer placeId, LocalDate date);
}
