package com.reservation.place;

import com.reservation.calendar.CalendarDay;
import com.reservation.table.TableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"tables", "days"})
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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
    private List<TableEntity> tables = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalendarDay> days = new ArrayList<>();

    public void addTable(TableEntity table) {
        if (tables == null) {
            tables = new ArrayList<>();
        }
        tables.add(table);
        table.setPlace(this);
    }

    public void addDay(CalendarDay day) {
        if (days == null) {
            days = new ArrayList<>();
        }
        days.add(day);
        day.setPlace(this);
    }
}
