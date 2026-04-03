package com.reservation.concurrency;


import com.reservation.dto.CreateReservationRequest;
import com.reservation.models.*;
import com.reservation.exceptions.ConflictException;
import com.reservation.repositories.*;
import com.reservation.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConcurrentReservationTest {

    @Autowired private ReservationService reservationService;
    @Autowired private UserRepository userRepository;
    @Autowired private ResourceRepository resourceRepository;

    private User user;
    private Resource resource;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Ibrahim");
        user.setEmail("ibrahim@gmail.com");
        userRepository.save(user);

        resource = new Resource();
        resource.setName("Salle A");
        resource.setActive(true);
        resourceRepository.save(resource);
    }

    @Test
    void shouldAllowOnlyOneReservationWhenTwoThreadsCompete() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // les deux threads partent exactement en même temps
                    CreateReservationRequest request = new CreateReservationRequest();
                    request.setUserId(user.getId());
                    request.setResourceId(resource.getId());
                    request.setStartTime(start);
                    request.setEndTime(end);
                    reservationService.create(request);
                    successCount.incrementAndGet();
                } catch (ConflictException e) {
                    failCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown(); // départ simultané
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Une seule réservation doit passer
        assertEquals(1, successCount.get(), "Une seule réservation doit être acceptée");
        assertEquals(1, failCount.get(), "Une réservation doit être rejetée");
    }
}