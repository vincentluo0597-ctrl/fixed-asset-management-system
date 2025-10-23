package com.example.gdzc.controller;

import com.example.gdzc.domain.FaultCase;
import com.example.gdzc.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge-cases")
@RequiredArgsConstructor
public class KnowledgeBaseController {
    private final KnowledgeBaseService knowledgeBaseService;

    @GetMapping("/page")
    public ResponseEntity<Page<FaultCase>> page(@RequestParam(required = false) Long equipmentId,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(required = false) String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(knowledgeBaseService.page(equipmentId, keyword, pageable));
    }

    @PostMapping
    public ResponseEntity<FaultCase> create(@RequestBody FaultCase faultCase) {
        return ResponseEntity.ok(knowledgeBaseService.create(faultCase));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        String[] parts = sort.split(",");
        String prop = parts[0].trim();
        Sort.Direction dir = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, prop));
    }
}