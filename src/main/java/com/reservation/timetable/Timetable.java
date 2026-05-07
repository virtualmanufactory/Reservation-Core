package com.reservation.timetable;

import com.reservation.opening_hours.OpeningHours;
import com.reservation.place.Place;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "timetables")
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne
    @JoinColumn(name = "opening_hours_id", nullable = false)
    private OpeningHours openingHours;

    private LocalDate date;

    private LocalDateTime time;

    @Column(name = "open_flag")
    private Boolean openFlag;

    private String slots;

    // Getters and setters
}
