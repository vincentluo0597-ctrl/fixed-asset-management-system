package com.example.gdzc.controller;

import com.example.gdzc.domain.BorrowRecord;
import com.example.gdzc.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {
    private final BorrowService borrowService;

    @GetMapping
    public ResponseEntity<Page<BorrowRecord>> page(@RequestParam(required = false) Long equipmentId,
                                                   @RequestParam(required = false) Long borrowerId,
                                                   @RequestParam(required = false) Boolean ongoingOnly,
                                                   @RequestParam(required = false) LocalDateTime borrowFrom,
                                                   @RequestParam(required = false) LocalDateTime borrowTo,
                                                   Pageable pageable) {
        Specification<BorrowRecord> spec = Specification.where(null);
        if (equipmentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("equipmentId"), equipmentId));
        }
        if (borrowerId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("borrowerId"), borrowerId));
        }
        if (Boolean.TRUE.equals(ongoingOnly)) {
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("returnDate")));
        }
        if (borrowFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("borrowDate"), borrowFrom));
        }
        if (borrowTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("borrowDate"), borrowTo));
        }
        return ResponseEntity.ok(borrowService.page(spec, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats(@RequestParam(required = false) LocalDate today,
                                                     @RequestParam(required = false) LocalDateTime start,
                                                     @RequestParam(required = false) LocalDateTime end) {
        long ongoing = borrowService.countOngoing();
        long overdue = borrowService.countOverdue(today != null ? today : LocalDate.now());
        long returned = (start != null && end != null) ? borrowService.countReturnedBetween(start, end) : 0L;
        Double avgHours = borrowService.averageDurationHours();
        return ResponseEntity.ok(Map.of(
                "ongoing", ongoing,
                "overdue", overdue,
                "returned", returned,
                "averageDurationHours", avgHours
        ));
    }

    @PostMapping
    public ResponseEntity<BorrowRecord> create(@RequestBody BorrowRecord record) {
        return ResponseEntity.ok(borrowService.create(record));
    }
}