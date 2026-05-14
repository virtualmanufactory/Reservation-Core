package com.reservation.calendar;

import com.reservation.orderer.Orderer;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface CalendarDayRepository  extends CrudRepository<CalendarDay, Long> {
    //TODO Znaleźć dostępną datę
    <Orderer> Optional findByPlaceAndDay(LocalDate date);

}
