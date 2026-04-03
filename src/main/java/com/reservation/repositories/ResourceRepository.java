package com.reservation.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import com.reservation.models.Resource;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
}