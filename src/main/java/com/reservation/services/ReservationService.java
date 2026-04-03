package com.reservation.services;

import com.reservation.dto.CreateReservationRequest;
import com.reservation.dto.ReservationResponse;
import com.reservation.models.*;
import com.reservation.exceptions.*;
import com.reservation.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            ResourceRepository resourceRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public ReservationResponse create(CreateReservationRequest request) {

        // StartTime doit être avant endTime
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new InvalidReservationException(
                "La date de début doit être antérieure à la date de fin."
            );
        }

        // Pas de réservation dans le passé
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new InvalidReservationException(
                "Impossible de réserver un créneau dans le passé."
            );
        }

        // L'utilisateur doit exister
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Utilisateur introuvable : " + request.getUserId()
            ));

        // La ressource doit exister et être active
        Resource resource = resourceRepository.findById(request.getResourceId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Ressource introuvable : " + request.getResourceId()
            ));

        if (!resource.isActive()) {
            throw new InvalidReservationException(
                "La ressource n'est pas disponible."
            );
        }

        // Pas de chevauchement — avec verrou pessimiste
        List<Reservation> overlapping = reservationRepository.findOverlapping(
            resource.getId(),
            request.getStartTime(),
            request.getEndTime(),
            ReservationStatus.CONFIRMED
        );

        if (!overlapping.isEmpty()) {
            throw new ConflictException(
                "Ce créneau est déjà réservé pour cette ressource."
            );
        }

        // Tout est valide : on crée la réservation
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public void cancel(UUID reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Réservation introuvable : " + reservationId
            ));

        // On ne peut pas annuler une réservation déjà commencée
        if (reservation.getStartTime().isBefore(LocalDateTime.now())) {
            throw new InvalidReservationException(
                "Impossible d'annuler une réservation déjà commencée."
            );
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidReservationException(
                "Cette réservation est déjà annulée."
            );
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public ReservationResponse getById(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Réservation introuvable : " + id
            ));

        return ReservationResponse.from(reservation);
    }
    
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAvailability(UUID resourceId) {
        resourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Ressource introuvable : " + resourceId
            ));

        return reservationRepository
            .findByResourceIdAndStatus(resourceId, ReservationStatus.CONFIRMED)
            .stream()
            .map(ReservationResponse::from)
            .toList();
    }
    
    @Transactional
    public ReservationResponse update(UUID reservationId, LocalDateTime newStart, LocalDateTime newEnd) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable : " + reservationId));

        if (reservation.getStartTime().isBefore(LocalDateTime.now())) {
            throw new InvalidReservationException("Impossible de modifier une réservation déjà commencée.");
        }

        if (!newStart.isBefore(newEnd)) {
            throw new InvalidReservationException("La date de début doit être avant la date de fin.");
        }

        // Vérifier chevauchement
        List<Reservation> overlapping = reservationRepository.findOverlapping(
            reservation.getResource().getId(),
            newStart,
            newEnd,
            ReservationStatus.CONFIRMED
        );

        // Exclure la réservation elle-même
        overlapping.removeIf(r -> r.getId().equals(reservationId));

        if (!overlapping.isEmpty()) {
            throw new ConflictException("Ce créneau est déjà réservé pour cette ressource.");
        }

        // Appliquer les changements
        reservation.setStartTime(newStart);
        reservation.setEndTime(newEnd);

        return ReservationResponse.from(reservationRepository.save(reservation));
    }
}