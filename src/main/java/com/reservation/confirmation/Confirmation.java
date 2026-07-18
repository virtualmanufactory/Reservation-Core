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

    /** Numer rezerwacji widoczny dla klienta, np. RES-20260718-A3F9B2 */
    @Column(name = "confirmation_code", nullable = false, unique = true)
    private String confirmationCode;

    @Column(nullable = false)
    private Boolean confirmed;

    @Column(nullable = false)
    private Boolean cancelled;
}
