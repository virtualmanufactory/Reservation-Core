package com.reservation.schedule;

import com.reservation.dto.ScheduleSlotDTO;
import com.reservation.dto.SlotAvailabilityUpdateDTO;
import com.reservation.place.Place;
import com.reservation.place.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleSlotRepository scheduleSlotRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public void generateYearSchedule(Place place, int year, LocalTime openFrom, LocalTime openTo, int tableCount) {
        LocalTime from = openFrom != null ? openFrom : LocalTime.of(12, 0);
        LocalTime to = openTo != null ? openTo : LocalTime.of(22, 0);
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("Godzina zamknięcia musi być późniejsza niż otwarcia");
        }

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<ScheduleSlot> batch = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            for (LocalTime hour = from; hour.isBefore(to); hour = hour.plusHours(1)) {
                ScheduleSlot slot = ScheduleSlot.builder()
                        .place(place)
                        .date(date)
                        .hour(hour)
                        .totalTables(tableCount)
                        .availableTables(tableCount)
                        .reservedTables(0)
                        .locked(false)
                        .build();
                batch.add(slot);
                if (batch.size() >= 500) {
                    scheduleSlotRepository.saveAll(batch);
                    batch.clear();
                }
            }
        }
        if (!batch.isEmpty()) {
            scheduleSlotRepository.saveAll(batch);
        }
    }

    public ScheduleSlot requireOpenSlot(Place place, LocalDate date, LocalTime startTime) {
        LocalTime hour = startTime.withMinute(0).withSecond(0).withNano(0);
        ScheduleSlot slot = scheduleSlotRepository.findByPlaceAndDateAndHour(place, date, hour)
                .orElseThrow(() -> new IllegalStateException("Brak slotu harmonogramu dla wybranego terminu"));

        if (slot.isLocked() || slot.getAvailableTables() <= 0) {
            throw new IllegalStateException("Wybrany termin jest niedostępny");
        }
        return slot;
    }

    @Transactional
    public void reserveTable(ScheduleSlot slot) {
        if (slot.getAvailableTables() <= 0) {
            throw new IllegalStateException("Brak dostępnych stolików w slocie");
        }
        slot.setAvailableTables(slot.getAvailableTables() - 1);
        slot.setReservedTables(slot.getReservedTables() + 1);
        scheduleSlotRepository.save(slot);
    }

    /** Po odwołaniu — zwiększ dostępność. */
    @Transactional
    public void releaseTable(Place place, LocalDate date, LocalTime startTime) {
        LocalTime hour = startTime.withMinute(0).withSecond(0).withNano(0);
        scheduleSlotRepository.findByPlaceAndDateAndHour(place, date, hour).ifPresent(slot -> {
            if (slot.getReservedTables() > 0) {
                slot.setReservedTables(slot.getReservedTables() - 1);
            }
            if (slot.getAvailableTables() < slot.getTotalTables()) {
                slot.setAvailableTables(slot.getAvailableTables() + 1);
            }
            scheduleSlotRepository.save(slot);
        });
    }

    public List<ScheduleSlotDTO> getDaySchedule(Integer placeId, LocalDate date) {
        return scheduleSlotRepository.findByPlaceIdAndDateOrderByHourAsc(placeId, date)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<ScheduleSlotDTO> getRange(Integer placeId, LocalDate from, LocalDate to) {
        return scheduleSlotRepository.findByPlaceIdAndDateBetweenOrderByDateAscHourAsc(placeId, from, to)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ScheduleSlotDTO updateAvailability(Integer placeId, SlotAvailabilityUpdateDTO dto) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono lokalu"));
        LocalTime hour = dto.hour().withMinute(0).withSecond(0).withNano(0);
        ScheduleSlot slot = scheduleSlotRepository.findByPlaceAndDateAndHour(place, dto.date(), hour)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono slotu"));

        if (dto.locked() != null) {
            slot.setLocked(dto.locked());
        }
        if (dto.availableTables() != null) {
            if (dto.availableTables() > slot.getTotalTables()) {
                throw new IllegalArgumentException("Dostępność nie może przekraczać liczby stolików");
            }
            slot.setAvailableTables(dto.availableTables());
            slot.setReservedTables(Math.max(0, slot.getTotalTables() - dto.availableTables()));
        }
        return toDto(scheduleSlotRepository.save(slot));
    }

    private ScheduleSlotDTO toDto(ScheduleSlot slot) {
        return new ScheduleSlotDTO(
                slot.getId(),
                slot.getDate(),
                slot.getHour(),
                slot.getTotalTables(),
                slot.getAvailableTables(),
                slot.getReservedTables(),
                slot.isLocked()
        );
    }
}
