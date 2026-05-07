package com.reservation.table;

import com.reservation.place.Place;
import com.reservation.timetable.Timetable;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "tables")
public class TableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer seats;

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @OneToMany(mappedBy = "tables")
    private Set<Place> places;


    // Getters and setters
}