package com.reservation.place;

import com.reservation.dto.PlaceDTO;
import com.reservation.dto.PlaceSetupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Validated
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping("/{placeId}/setup")
    public ResponseEntity<PlaceDTO> setupPlace(
            @PathVariable Long placeId,
            @RequestBody PlaceSetupRequestDto request
    ) {
        return ResponseEntity.ok(placeService.setupPlace(placeId, request));
    }
}
