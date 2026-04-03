package com.reservation.dto;

import com.reservation.models.Reservation;
import com.reservation.models.ReservationStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReservationResponse {

    private UUID id;
    private UUID userId;
    private UUID resourceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    // On construit la réponse depuis l'entité
    public static ReservationResponse from(Reservation reservation) {
        ReservationResponse dto = new ReservationResponse();
        dto.id = reservation.getId();
        dto.userId = reservation.getUser().getId();
        dto.resourceId = reservation.getResource().getId();
        dto.startTime = reservation.getStartTime();
        dto.endTime = reservation.getEndTime();
        dto.status = reservation.getStatus();
        dto.createdAt = reservation.getCreatedAt();
        return dto;
    }
}