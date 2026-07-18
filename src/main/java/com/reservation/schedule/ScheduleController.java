package com.reservation.schedule;

import com.reservation.dto.ScheduleSlotDTO;
import com.reservation.dto.SlotAvailabilityUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/places/{placeId}/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public List<ScheduleSlotDTO> daySchedule(
            @PathVariable Integer placeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.getDaySchedule(placeId, date);
    }

    @GetMapping("/range")
    public List<ScheduleSlotDTO> range(
            @PathVariable Integer placeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return scheduleService.getRange(placeId, from, to);
    }

    @PatchMapping("/availability")
    public ScheduleSlotDTO updateAvailability(
            @PathVariable Integer placeId,
            @Valid @RequestBody SlotAvailabilityUpdateDTO dto) {
        return scheduleService.updateAvailability(placeId, dto);
    }
}
