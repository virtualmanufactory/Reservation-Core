package com.reservation.table;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TableRepository extends CrudRepository<TableEntity, Integer> {
    @Query("""
        SELECT t
        FROM TableEntity t
        WHERE t.place.id = :placeId
          AND t.active = true
          AND t.seats >= :peopleCount
          AND NOT EXISTS (
              SELECT r
              FROM Reservation r
              WHERE r.table = t
                AND r.date = :date
                AND r.status = com.reservation.reservation.ReservationStatus.ACTIVE
                AND :startTime < r.endTime
                AND :endTime > r.startTime
          )
        ORDER BY t.seats ASC
    """)
    List<TableEntity> findAvailableTables(
            @Param("placeId") Integer placeId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("peopleCount") Integer peopleCount
    );

    default Optional<TableEntity> findFirstAvailableTable(
            Integer placeId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer peopleCount
    ) {
        return findAvailableTables(placeId, date, startTime, endTime, peopleCount)
                .stream()
                .findFirst();
    }

    long countByPlaceIdAndActiveTrue(Integer placeId);
}
