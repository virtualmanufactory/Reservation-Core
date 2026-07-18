package com.reservation.schedule;

import com.reservation.place.Place;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Harmonogram: data + godzina + liczba dostępnych / zarezerwowanych stolików.
 */
@Entity
@Table(name = "schedule_slots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"place_id", "slot_date", "slot_hour"}))
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "slot_date", nullable = false)
    private LocalDate date;

    @Column(name = "slot_hour", nullable = false)
    private LocalTime hour;

    @Column(name = "total_tables", nullable = false)
    private Integer totalTables;

    @Column(name = "available_tables", nullable = false)
    private Integer availableTables;

    @Column(name = "reserved_tables", nullable = false)
    private Integer reservedTables;

    /** Sterowanie dostępnością — zablokowany slot nie przyjmuje rezerwacji. */
    @Column(nullable = false)
    private boolean locked;
}
