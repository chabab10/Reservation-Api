package com.reservation.services;

import com.reservation.models.Resource;
import com.reservation.repositories.ResourceRepository;
import com.reservation.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public Resource create(Resource resource) {
        return resourceRepository.save(resource);
    }

    @Transactional(readOnly = true)
    public List<Resource> findAll() {
        return resourceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Resource findById(UUID id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }

    @Transactional
    public Resource update(UUID id, Resource updated) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        resource.setName(updated.getName());
        resource.setDescription(updated.getDescription());
        resource.setActive(updated.isActive());

        return resourceRepository.save(resource);
    }

    @Transactional
    public void delete(UUID id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        }
        resourceRepository.deleteById(id);
    }
}