package com.example.gdzc.controller;

import com.example.gdzc.domain.MaintenanceRecord;
import com.example.gdzc.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    @GetMapping
    public ResponseEntity<Page<MaintenanceRecord>> page(@RequestParam(required = false) Long equipmentId,
                                                        @RequestParam(required = false) Boolean ongoingOnly,
                                                        @RequestParam(required = false) LocalDateTime startFrom,
                                                        @RequestParam(required = false) LocalDateTime startTo,
                                                        Pageable pageable) {
        Specification<MaintenanceRecord> spec = Specification.where(null);
        if (equipmentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("equipmentId"), equipmentId));
        }
        if (Boolean.TRUE.equals(ongoingOnly)) {
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("endAt")));
        }
        if (startFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startAt"), startFrom));
        }
        if (startTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startAt"), startTo));
        }
        return ResponseEntity.ok(maintenanceService.page(spec, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats(@RequestParam(required = false) LocalDateTime start,
                                                     @RequestParam(required = false) LocalDateTime end) {
        long ongoing = maintenanceService.countOngoing();
        long completed = (start != null && end != null) ? maintenanceService.countCompletedBetween(start, end) : 0L;
        Double avgHours = maintenanceService.averageDurationHours();
        return ResponseEntity.ok(Map.of(
                "ongoing", ongoing,
                "completed", completed,
                "averageDurationHours", avgHours
        ));
    }

    @PostMapping
    public ResponseEntity<MaintenanceRecord> create(@RequestBody MaintenanceRecord record) {
        return ResponseEntity.ok(maintenanceService.create(record));
    }
}