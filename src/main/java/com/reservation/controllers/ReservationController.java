package com.reservation.controllers;

import com.reservation.dto.CreateReservationRequest;
import com.reservation.dto.ReservationResponse;
import com.reservation.services.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody CreateReservationRequest request) {
        return reservationService.create(request);
    }

    // DELETE (cancel)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID id) {
        reservationService.cancel(id);
    }

    @GetMapping
    public List<ReservationResponse> getAll() {
        return reservationService.getAll();
    }

    @GetMapping("/{id}")
    public ReservationResponse getById(@PathVariable UUID id) {
        return reservationService.getById(id);
    }

    @GetMapping("/resources/{resourceId}/availability")
    public List<ReservationResponse> getAvailability(@PathVariable UUID resourceId) {
        return reservationService.getAvailability(resourceId);
    }
    
    @PutMapping("/{id}")
    public ReservationResponse updateReservation(
            @PathVariable UUID id,
            @RequestBody CreateReservationRequest request) { 
        return reservationService.update(id, request.getStartTime(), request.getEndTime());
    }
}