package com.reservation.analytics;

import com.reservation.dto.AnalyticsDTO;
import com.reservation.place.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ReservationAnalyticsRepository analyticsRepository;

    @Transactional
    public void recordReservation(Place place, LocalDate date, LocalTime startTime, int peopleCount) {
        ReservationAnalytics row = getOrCreate(place, date, startTime);
        row.setReservedTablesCount(row.getReservedTablesCount() + 1);
        row.setPeopleCount(row.getPeopleCount() + peopleCount);
        analyticsRepository.save(row);
    }

    @Transactional
    public void recordCancellation(Place place, LocalDate date, LocalTime startTime, int peopleCount) {
        ReservationAnalytics row = getOrCreate(place, date, startTime);
        if (row.getReservedTablesCount() > 0) {
            row.setReservedTablesCount(row.getReservedTablesCount() - 1);
        }
        row.setCancelledTablesCount(row.getCancelledTablesCount() + 1);
        row.setPeopleCount(Math.max(0, row.getPeopleCount() - peopleCount));
        analyticsRepository.save(row);
    }

    public List<AnalyticsDTO> getDay(Integer placeId, LocalDate date) {
        return analyticsRepository.findByPlaceIdAndDateOrderByHourAsc(placeId, date)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<AnalyticsDTO> getRange(Integer placeId, LocalDate from, LocalDate to) {
        return analyticsRepository.findByPlaceIdAndDateBetweenOrderByDateAscHourAsc(placeId, from, to)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ReservationAnalytics getOrCreate(Place place, LocalDate date, LocalTime startTime) {
        LocalTime hour = startTime.withMinute(0).withSecond(0).withNano(0);
        return analyticsRepository.findByPlaceAndDateAndHour(place, date, hour)
                .orElseGet(() -> ReservationAnalytics.builder()
                        .place(place)
                        .date(date)
                        .hour(hour)
                        .reservedTablesCount(0)
                        .cancelledTablesCount(0)
                        .peopleCount(0)
                        .build());
    }

    private AnalyticsDTO toDto(ReservationAnalytics row) {
        return new AnalyticsDTO(
                row.getDate(),
                row.getHour(),
                row.getReservedTablesCount(),
                row.getCancelledTablesCount(),
                row.getPeopleCount(),
                row.getUpdatedAt()
        );
    }
}
