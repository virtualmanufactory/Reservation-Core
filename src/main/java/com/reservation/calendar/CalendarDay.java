package com.reservation.calendar;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "calendar_days",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"date", "place_id"})})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalendarDay {

}
