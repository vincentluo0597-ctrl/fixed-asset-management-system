package com.example.gdzc.controller;

import com.example.gdzc.domain.EquipmentCategory;
import com.example.gdzc.domain.EquipmentType;
import com.example.gdzc.service.EquipmentCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment-categories")
@RequiredArgsConstructor
public class EquipmentCategoryController {
    private final EquipmentCategoryService equipmentCategoryService;

    @GetMapping
    public ResponseEntity<List<EquipmentCategory>> list() {
        return ResponseEntity.ok(equipmentCategoryService.findActiveCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentCategory> get(@PathVariable Long id) {
        return equipmentCategoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/roots")
    public ResponseEntity<List<EquipmentCategory>> roots() {
        return ResponseEntity.ok(equipmentCategoryService.findRootCategories());
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<EquipmentCategory>> byParent(@PathVariable Long parentId) {
        return ResponseEntity.ok(equipmentCategoryService.findByParentId(parentId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<EquipmentCategory>> byType(@PathVariable EquipmentType type) {
        return ResponseEntity.ok(equipmentCategoryService.findByType(type));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<EquipmentCategory>> tree() {
        return ResponseEntity.ok(equipmentCategoryService.buildCategoryTree());
    }

    @PostMapping
    public ResponseEntity<EquipmentCategory> create(@Valid @RequestBody EquipmentCategory category) {
        return ResponseEntity.ok(equipmentCategoryService.create(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentCategory> update(@PathVariable Long id, @Valid @RequestBody EquipmentCategory category) {
        return ResponseEntity.ok(equipmentCategoryService.update(id, category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}