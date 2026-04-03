package com.reservation.repositories;


import com.reservation.models.Reservation;
import com.reservation.models.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    // Détecte si un créneau chevauche une réservation existante sur la même ressource
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.resource.id = :resourceId
        AND r.status = :status
        AND r.startTime < :endTime
        AND r.endTime > :startTime
    """)
    List<Reservation> findOverlapping(
        @Param("resourceId") UUID resourceId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("status") ReservationStatus status
    );
    
    // Récupère toutes les réservations d'une ressource (pour la disponibilité)
    List<Reservation> findByResourceIdAndStatus(UUID resourceId, ReservationStatus status);
}