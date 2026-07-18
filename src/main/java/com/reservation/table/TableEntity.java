package com.reservation.table;

import com.reservation.place.Place;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tables")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "place")
public class TableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column
    private Integer seats;

    @Column
    private Integer number;

    private boolean active;

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;
}
