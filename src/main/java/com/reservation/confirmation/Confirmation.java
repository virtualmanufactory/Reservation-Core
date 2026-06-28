package com.reservation.confirmation;

import com.reservation.reservation.Reservation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(nullable = false)
    private Boolean confirmed;

    @Column(nullable = false)
    private Boolean cancelled;

    // Getters and setters
}
