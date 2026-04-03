package com.reservation.services;

import com.reservation.dto.CreateReservationRequest;
import com.reservation.models.*;
import com.reservation.exceptions.*;
import com.reservation.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock 
    private ReservationRepository reservationRepository;
    
    @Mock 
    private UserRepository userRepository;
    
    @Mock 
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private Resource resource;
    private CreateReservationRequest validRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Ahmed");
        user.setEmail("ahmed@gmail.com");

        resource = new Resource();
        resource.setName("Salle C");
        resource.setActive(true);

        validRequest = new CreateReservationRequest();
        validRequest.setUserId(UUID.randomUUID());
        validRequest.setResourceId(UUID.randomUUID());
        validRequest.setStartTime(LocalDateTime.now().plusHours(1));
        validRequest.setEndTime(LocalDateTime.now().plusHours(2));
    }

    @Test
    void shouldThrowWhenStartIsAfterEnd() {
        validRequest.setStartTime(LocalDateTime.now().plusHours(3));
        validRequest.setEndTime(LocalDateTime.now().plusHours(1));

        assertThrows(InvalidReservationException.class,
            () -> reservationService.create(validRequest));
    }

    @Test
    void shouldThrowWhenStartIsInThePast() {
        validRequest.setStartTime(LocalDateTime.now().minusHours(1));
        validRequest.setEndTime(LocalDateTime.now().plusHours(1));

        assertThrows(InvalidReservationException.class,
            () -> reservationService.create(validRequest));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> reservationService.create(validRequest));
    }

    @Test
    void shouldThrowWhenResourceIsInactive() {
        resource.setActive(false);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(any())).thenReturn(Optional.of(resource));

        assertThrows(InvalidReservationException.class,
            () -> reservationService.create(validRequest));
    }

    @Test
    void shouldThrowWhenSlotIsAlreadyTaken() {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(any())).thenReturn(Optional.of(resource));
        when(reservationRepository.findOverlapping(any(), any(), any(), any()))
            .thenReturn(List.of(new Reservation()));

        assertThrows(ConflictException.class,
            () -> reservationService.create(validRequest));
    }

    @Test
    void shouldCreateReservationWhenEverythingIsValid() {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(resourceRepository.findById(any())).thenReturn(Optional.of(resource));
        when(reservationRepository.findOverlapping(any(), any(), any(), any()))
            .thenReturn(List.of());

        Reservation saved = new Reservation();
        saved.setUser(user);
        saved.setResource(resource);
        saved.setStartTime(validRequest.getStartTime());
        saved.setEndTime(validRequest.getEndTime());
        saved.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.save(any())).thenReturn(saved);

        assertDoesNotThrow(() -> reservationService.create(validRequest));
        verify(reservationRepository, times(1)).save(any());
    }
}