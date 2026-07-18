package com.reservation.schedule;

import com.reservation.place.Place;
import com.reservation.place.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleSlotRepository scheduleSlotRepository;
    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private Place place;
    private ScheduleSlot slot;

    @BeforeEach
    void setUp() {
        place = Place.builder().id(1).name("Bistro").initialized(true).build();
        slot = ScheduleSlot.builder()
                .id(10)
                .place(place)
                .date(LocalDate.of(2026, 8, 1))
                .hour(LocalTime.of(13, 0))
                .totalTables(3)
                .availableTables(3)
                .reservedTables(0)
                .locked(false)
                .build();
    }

    @Test
    void reserveTableDecrementsAvailability() {
        scheduleService.reserveTable(slot);

        assertThat(slot.getAvailableTables()).isEqualTo(2);
        assertThat(slot.getReservedTables()).isEqualTo(1);
        verify(scheduleSlotRepository).save(slot);
    }

    @Test
    void reserveTableFailsWhenNoAvailability() {
        slot.setAvailableTables(0);

        assertThatThrownBy(() -> scheduleService.reserveTable(slot))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Brak dostępnych stolików");
    }

    @Test
    void releaseTableIncreasesAvailability() {
        slot.setAvailableTables(1);
        slot.setReservedTables(2);
        when(scheduleSlotRepository.findByPlaceAndDateAndHour(place, slot.getDate(), LocalTime.of(13, 0)))
                .thenReturn(Optional.of(slot));

        scheduleService.releaseTable(place, slot.getDate(), LocalTime.of(13, 15));

        assertThat(slot.getAvailableTables()).isEqualTo(2);
        assertThat(slot.getReservedTables()).isEqualTo(1);
        verify(scheduleSlotRepository).save(slot);
    }

    @Test
    void requireOpenSlotRejectsLockedSlot() {
        slot.setLocked(true);
        when(scheduleSlotRepository.findByPlaceAndDateAndHour(place, slot.getDate(), LocalTime.of(13, 0)))
                .thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> scheduleService.requireOpenSlot(place, slot.getDate(), LocalTime.of(13, 30)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("niedostępny");
    }

    @Test
    void generateYearSchedulePersistsHourlySlots() {
        when(scheduleSlotRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        scheduleService.generateYearSchedule(
                place, 2026, LocalTime.of(12, 0), LocalTime.of(14, 0), 2);

        ArgumentCaptor<List<ScheduleSlot>> captor = ArgumentCaptor.forClass(List.class);
        verify(scheduleSlotRepository, atLeastOnce()).saveAll(captor.capture());

        List<ScheduleSlot> all = captor.getAllValues().stream().flatMap(List::stream).toList();
        assertThat(all).isNotEmpty();
        assertThat(all.getFirst().getTotalTables()).isEqualTo(2);
        assertThat(all.getFirst().getAvailableTables()).isEqualTo(2);
        assertThat(all).allMatch(s -> !s.getHour().isBefore(LocalTime.of(12, 0)));
        assertThat(all).allMatch(s -> s.getHour().isBefore(LocalTime.of(14, 0)));
    }
}
