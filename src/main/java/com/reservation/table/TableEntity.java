package com.reservation.table;

import com.reservation.place.Place;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "tables")
@Builder
@AllArgsConstructor
@Data
public class TableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private Integer seats;

    @Column
    private Integer number;

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;





    // Getters and setters
}