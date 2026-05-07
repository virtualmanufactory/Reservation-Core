package com.reservation.confirmation;

import com.reservation.reservation.Reservation;
import jakarta.persistence.*;

@Entity
@Table(name = "confirmations")
public class Confirmation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    private Boolean confirmed;
    private Boolean cancelled;

    // Getters and setters
}
