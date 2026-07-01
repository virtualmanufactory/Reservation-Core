package com.reservation.place;

import com.reservation.calendar.CalendarDay;
import com.reservation.dto.PlaceDTO;
import com.reservation.dto.PlaceSetupRequestDto;
import com.reservation.dto.TableSetupDto;
import com.reservation.table.TableEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceService {

    private final PlaceRepository placeRepository;

    public PlaceDTO setupPlace(Long placeId, PlaceSetupRequestDto request) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("Place not found"));

        if (place.isInitialized()) {
            throw new RuntimeException("Place already initialized");
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

        if (request.tables() != null) {
            for (TableSetupDto tableDto : request.tables()) {
                TableEntity table = new TableEntity();
                table.setNumber(tableDto.number());
                table.setSeats(tableDto.seatsCount());
                table.setActive(tableDto.active());

                place.addTable(table);
            }
        }

        place.setInitialized(true);

        Place saved = placeRepository.save(place);

        return new PlaceDTO(
                saved.getId(),
                saved.getName(),
                saved.getCity(),
                saved.getStreet(),
                saved.getStreetNumber(),
                saved.getPostalCode(),
                saved.getPostOffice(),
                saved.getDays().size(),
                saved.getTables().size()
        );
    }
}
