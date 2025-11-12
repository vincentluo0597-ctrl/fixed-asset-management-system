package com.example.gdzc.controller;

import com.example.gdzc.domain.SparePart;
import com.example.gdzc.domain.SparePartModelLink;
import com.example.gdzc.domain.SparePartTransaction;
import com.example.gdzc.service.SparePartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spare-parts")
@RequiredArgsConstructor
public class SparePartController {
    /**
     * 备件管理控制器：分页查询、按设备型号关联查询、库存调整与统计。
     */
    private final SparePartService sparePartService;

    @GetMapping("/page")
    public ResponseEntity<Page<SparePart>> page(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Boolean lowStockOnly,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(required = false) String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(sparePartService.page(keyword, lowStockOnly, pageable));
    }

    @GetMapping("/by-model")
    public ResponseEntity<List<SparePartModelLink>> byModel(@RequestParam String model) {
        return ResponseEntity.ok(sparePartService.findLinksByModel(model));
    }

    @PostMapping("/{id}/adjust-stock")
    public ResponseEntity<SparePart> adjustStock(@PathVariable Long id,
                                                 @RequestParam Long locationId,
                                                 @RequestParam int quantity,
                                                 @RequestParam SparePartTransaction.TransactionType type,
                                                 @RequestParam(required = false) String note,
                                                 @RequestParam(required = false) Long refMaintenanceId) {
        return ResponseEntity.ok(sparePartService.adjustStock(id, locationId, quantity, type, note, refMaintenanceId));
    }

    @GetMapping("/stats")
    public ResponseEntity<SparePartService.SparePartStats> stats() {
        return ResponseEntity.ok(sparePartService.stats());
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
