package com.reservation.cancellation;

import com.reservation.confirmation.Confirmation;
import jakarta.persistence.*;

@Entity
@Table(name = "cancellations")
public class Cancellation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "confirmation_id", nullable = false)
    private Confirmation confirmation;

    private Boolean confirmed;

    // Getters and setters
}
