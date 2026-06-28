package com.reservation.place;

import com.reservation.calendar.CalendarDay;
import com.reservation.table.TableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String city;
    private String street;

    @Column(name = "street_number")
    private Integer streetNumber;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "post_office")
    private String postOffice;

    @OneToMany(mappedBy = "place")
    private Set<TableEntity> tables = new HashSet<>();

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CalendarDay> days = new HashSet<>();
}
