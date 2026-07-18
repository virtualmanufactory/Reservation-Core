package com.reservation.opening_hours;

import com.reservation.util.Weekdays;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "opening_hours")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpeningHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Weekdays weekdays;

    private LocalTime openTime;

    private LocalTime closeTime;

    @Column(nullable = false)
    private boolean closed = false;
}
