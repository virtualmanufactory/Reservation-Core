package com.reservation.place;

import com.reservation.table.TableEntity;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "places")
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

    @OneToMany(mappedBy = "places")
    private Set<TableEntity> tables;
    // Getters and setters
}
