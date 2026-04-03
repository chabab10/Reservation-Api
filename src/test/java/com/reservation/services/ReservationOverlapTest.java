package com.reservation.services;

import com.reservation.dto.CreateReservationRequest;
import com.reservation.models.*;
import com.reservation.exceptions.ConflictException;
import com.reservation.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReservationOverlapTest {

    @Autowired 
    private ReservationService reservationService;
    
    @Autowired 
    private UserRepository userRepository;
    
    @Autowired 
    private ResourceRepository resourceRepository;

    private User user;
    private Resource resource;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Chabab");
        user.setEmail("chabab@gmail.com");
        userRepository.save(user);

        resource = new Resource();
        resource.setName("Salle B");
        resource.setActive(true);
        resourceRepository.save(resource);
    }

    @Test
    void shouldRejectOverlappingReservation() {
        // Première réservation : 10h → 12h
        CreateReservationRequest first = new CreateReservationRequest();
        first.setUserId(user.getId());
        first.setResourceId(resource.getId());
        first.setStartTime(LocalDateTime.now().plusHours(1));
        first.setEndTime(LocalDateTime.now().plusHours(3));
        reservationService.create(first);

        // Deuxième réservation sur le même créneau : doit échouer
        CreateReservationRequest second = new CreateReservationRequest();
        second.setUserId(user.getId());
        second.setResourceId(resource.getId());
        second.setStartTime(LocalDateTime.now().plusHours(2));
        second.setEndTime(LocalDateTime.now().plusHours(4));

        assertThrows(ConflictException.class,
            () -> reservationService.create(second));
    }

    @Test
    void shouldAllowNonOverlappingReservations() {
        // Première réservation : +1h → +2h
        CreateReservationRequest first = new CreateReservationRequest();
        first.setUserId(user.getId());
        first.setResourceId(resource.getId());
        first.setStartTime(LocalDateTime.now().plusHours(1));
        first.setEndTime(LocalDateTime.now().plusHours(2));
        reservationService.create(first);

        // Deuxième réservation juste après : +2h → +3h
        CreateReservationRequest second = new CreateReservationRequest();
        second.setUserId(user.getId());
        second.setResourceId(resource.getId());
        second.setStartTime(LocalDateTime.now().plusHours(2));
        second.setEndTime(LocalDateTime.now().plusHours(3));

        assertDoesNotThrow(() -> reservationService.create(second));
    }
}