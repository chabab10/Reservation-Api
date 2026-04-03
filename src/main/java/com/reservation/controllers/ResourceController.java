package com.reservation.controllers;

import com.reservation.models.Resource;
import com.reservation.services.ResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Resource create(@Valid @RequestBody Resource resource) {
        return resourceService.create(resource);
    }

    @GetMapping
    public List<Resource> findAll() {
        return resourceService.findAll();
    }

    @GetMapping("/{id}")
    public Resource findById(@PathVariable UUID id) {
        return resourceService.findById(id);
    }

    @PutMapping("/{id}")
    public Resource update(@PathVariable UUID id, @RequestBody Resource updated) {
        return resourceService.update(id, updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        resourceService.delete(id);
    }
}
