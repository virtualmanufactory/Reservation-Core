package com.reservation.place;

import com.reservation.calendar.CalendarDay;
import com.reservation.dto.CreatePlaceDTO;
import com.reservation.dto.PlaceDTO;
import com.reservation.dto.PlaceSetupRequestDto;
import com.reservation.dto.TableSetupDto;
import com.reservation.schedule.ScheduleService;
import com.reservation.table.TableEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final ScheduleService scheduleService;

    public PlaceDTO createPlace(CreatePlaceDTO dto) {
        Place place = Place.builder()
                .name(dto.name())
                .city(dto.city())
                .street(dto.street())
                .streetNumber(dto.streetNumber())
                .postalCode(dto.postalCode())
                .postOffice(dto.postOffice())
                .initialized(false)
                .build();
        Place saved = placeRepository.save(place);
        return toDto(saved);
    }

    public List<PlaceDTO> listPlaces() {
        return placeRepository.findAll().stream().map(this::toDto).toList();
    }

    public PlaceDTO getPlace(Integer placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono lokalu"));
        return toDto(place);
    }

    public PlaceDTO setupPlace(Integer placeId, PlaceSetupRequestDto request) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono lokalu"));

        if (place.isInitialized()) {
            throw new IllegalStateException("Lokal został już zainicjalizowany");
        }

        LocalDate start = LocalDate.of(request.year(), 1, 1);
        LocalDate end = LocalDate.of(request.year(), 12, 31);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            CalendarDay day = CalendarDay.builder()
                    .date(date)
                    .isLocked(false)
                    .build();
            place.addDay(day);
        }

        int activeTables = 0;
        if (request.tables() != null) {
            for (TableSetupDto tableDto : request.tables()) {
                TableEntity table = new TableEntity();
                table.setNumber(tableDto.number());
                table.setSeats(tableDto.seatsCount());
                table.setActive(tableDto.active());
                place.addTable(table);
                if (tableDto.active()) {
                    activeTables++;
                }
            }
        }

        place.setInitialized(true);
        Place saved = placeRepository.save(place);

        LocalTime openFrom = request.defaultOpenFrom() != null ? request.defaultOpenFrom() : LocalTime.of(12, 0);
        LocalTime openTo = request.defaultOpenTo() != null ? request.defaultOpenTo() : LocalTime.of(22, 0);
        scheduleService.generateYearSchedule(saved, request.year(), openFrom, openTo, activeTables);

        return toDto(saved);
    }

    /** Aktualizacja podstawowych danych lokalu. */
    public PlaceDTO updatePlace(Integer placeId, CreatePlaceDTO dto) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono lokalu"));
        place.setName(dto.name());
        place.setCity(dto.city());
        place.setStreet(dto.street());
        place.setStreetNumber(dto.streetNumber());
        place.setPostalCode(dto.postalCode());
        place.setPostOffice(dto.postOffice());
        return toDto(placeRepository.save(place));
    }

    private PlaceDTO toDto(Place place) {
        int days = place.getDays() != null ? place.getDays().size() : 0;
        int tables = place.getTables() != null ? place.getTables().size() : 0;
        return new PlaceDTO(
                place.getId(),
                place.getName(),
                place.getCity(),
                place.getStreet(),
                place.getStreetNumber(),
                place.getPostalCode(),
                place.getPostOffice(),
                days,
                tables
        );
    }
}
