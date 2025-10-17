package com.example.gdzc.controller;

import com.example.gdzc.domain.Location;
import com.example.gdzc.dto.LocationDTO;
import com.example.gdzc.dto.LocationTreeDTO;
import com.example.gdzc.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<Location>> list() {
        return ResponseEntity.ok(locationService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getById(@PathVariable Long id) {
        return locationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<Location>> children(@PathVariable Long parentId) {
        return ResponseEntity.ok(locationService.children(parentId));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<LocationTreeDTO>> tree() {
        return ResponseEntity.ok(locationService.buildTree());
    }

    @PostMapping
    public ResponseEntity<Location> create(@Valid @RequestBody LocationDTO dto) {
        return ResponseEntity.ok(locationService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> update(@PathVariable Long id, @Valid @RequestBody LocationDTO dto) {
        return ResponseEntity.ok(locationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}