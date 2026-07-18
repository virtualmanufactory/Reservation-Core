package com.reservation.analytics;

import com.reservation.place.Place;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Osobna tabela analityki: dzień, godzina, liczba zarezerwowanych stolików.
 */
@Entity
@Table(name = "reservation_analytics",
        uniqueConstraints = @UniqueConstraint(columnNames = {"place_id", "analytics_date", "analytics_hour"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "place")
public class ReservationAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "analytics_date", nullable = false)
    private LocalDate date;

    @Column(name = "analytics_hour", nullable = false)
    private LocalTime hour;

    @Column(name = "reserved_tables_count", nullable = false)
    private Integer reservedTablesCount;

    @Column(name = "cancelled_tables_count", nullable = false)
    private Integer cancelledTablesCount;

    @Column(name = "people_count", nullable = false)
    private Integer peopleCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
        if (reservedTablesCount == null) {
            reservedTablesCount = 0;
        }
        if (cancelledTablesCount == null) {
            cancelledTablesCount = 0;
        }
        if (peopleCount == null) {
            peopleCount = 0;
        }
    }
}
