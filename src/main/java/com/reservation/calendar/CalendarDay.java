package com.reservation.calendar;

import com.reservation.place.Place;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "calendar_days",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"date", "place_id"})})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalendarDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private boolean isLocked;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Place place;


}
