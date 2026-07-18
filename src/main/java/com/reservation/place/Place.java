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

    private boolean initialized;

    @Builder.Default
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TableEntity> tables = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CalendarDay> days = new HashSet<>();

    public void addTable(TableEntity table) {
        if (tables == null) {
            tables = new HashSet<>();
        }
        tables.add(table);
        table.setPlace(this);
    }

    public void addDay(CalendarDay day) {
        if (days == null) {
            days = new HashSet<>();
        }
        days.add(day);
        day.setPlace(this);
    }
}
