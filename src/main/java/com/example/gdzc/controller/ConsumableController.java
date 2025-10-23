package com.example.gdzc.controller;

import com.example.gdzc.domain.Consumable;
import com.example.gdzc.domain.ConsumableReplacementRecord;
import com.example.gdzc.service.ConsumableService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consumables")
@RequiredArgsConstructor
public class ConsumableController {
    private final ConsumableService consumableService;

    @GetMapping("/page")
    public ResponseEntity<Page<Consumable>> page(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Boolean lowStockOnly,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(consumableService.page(keyword, lowStockOnly, pageable));
    }

    @PostMapping("/replacement")
    public ResponseEntity<ConsumableReplacementRecord> replacement(@RequestParam Long consumableId,
                                                                   @RequestParam Long equipmentId,
                                                                   @RequestParam(required = false) Integer usageValue,
                                                                   @RequestParam(required = false) String note) {
        return ResponseEntity.ok(consumableService.recordReplacement(consumableId, equipmentId, usageValue, note));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        }
        String[] parts = sort.split(",");
        String prop = parts[0].trim();
        Sort.Direction dir = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(dir, prop));
    }
}