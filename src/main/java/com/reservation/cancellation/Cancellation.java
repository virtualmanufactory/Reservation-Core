package com.reservation.cancellation;

import com.reservation.confirmation.Confirmation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "cancellations")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "confirmation_id", nullable = false, unique = true)
    private Confirmation confirmation;

    @Column(nullable = false)
    private Boolean confirmed;

    @Column(name = "cancelled_at", nullable = false)
    private Instant cancelledAt;

    @Column(length = 500)
    private String reason;

    @PrePersist
    void onCreate() {
        if (cancelledAt == null) {
            cancelledAt = Instant.now();
        }
        if (confirmed == null) {
            confirmed = true;
        }
    }
}
