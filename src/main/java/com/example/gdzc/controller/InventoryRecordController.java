package com.example.gdzc.controller;

import com.example.gdzc.domain.InventoryRecord;
import com.example.gdzc.dto.InventoryRecordDTO;
import com.example.gdzc.service.InventoryRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-records")
@RequiredArgsConstructor
public class InventoryRecordController {
    private final InventoryRecordService inventoryRecordService;

    @GetMapping
    public ResponseEntity<List<InventoryRecord>> list(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        return ResponseEntity.ok(inventoryRecordService.listByEquipmentId(equipmentId, limit));
    }

    @PostMapping("/{equipmentId}")
    public ResponseEntity<InventoryRecord> create(@PathVariable Long equipmentId, @Valid @RequestBody InventoryRecordDTO dto) {
        return ResponseEntity.ok(inventoryRecordService.create(equipmentId, dto));
    }
}