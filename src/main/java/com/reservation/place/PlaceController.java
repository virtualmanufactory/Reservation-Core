package com.reservation.place;

import com.reservation.dto.CreatePlaceDTO;
import com.reservation.dto.PlaceDTO;
import com.reservation.dto.PlaceSetupRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceDTO createPlace(@Valid @RequestBody CreatePlaceDTO dto) {
        return placeService.createPlace(dto);
    }

    @GetMapping
    public List<PlaceDTO> listPlaces() {
        return placeService.listPlaces();
    }

    @GetMapping("/{placeId}")
    public PlaceDTO getPlace(@PathVariable Integer placeId) {
        return placeService.getPlace(placeId);
    }

    @PutMapping("/{placeId}")
    public PlaceDTO updatePlace(@PathVariable Integer placeId, @Valid @RequestBody CreatePlaceDTO dto) {
        return placeService.updatePlace(placeId, dto);
    }

    @PostMapping("/{placeId}/setup")
    public ResponseEntity<PlaceDTO> setupPlace(
            @PathVariable Integer placeId,
            @Valid @RequestBody PlaceSetupRequestDto request
    ) {
        return ResponseEntity.ok(placeService.setupPlace(placeId, request));
    }
}
