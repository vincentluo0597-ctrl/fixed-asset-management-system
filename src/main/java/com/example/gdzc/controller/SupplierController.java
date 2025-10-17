package com.example.gdzc.controller;

import com.example.gdzc.domain.Supplier;
import com.example.gdzc.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<List<Supplier>> list(Pageable pageable) {
        // 如果提供了分页参数则返回分页，否则返回全部
        if (pageable.getPageSize() > 0) {
            Page<Supplier> page = supplierService.findAll(pageable);
            return ResponseEntity.ok(page.getContent());
        }
        return ResponseEntity.ok(supplierService.findActiveSuppliers());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Supplier>> page(Pageable pageable) {
        return ResponseEntity.ok(supplierService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> get(@PathVariable Long id) {
        return supplierService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Supplier>> search(@RequestParam("keyword") String keyword, Pageable pageable) {
        return ResponseEntity.ok(supplierService.searchByKeyword(keyword, pageable));
    }

    @PostMapping
    public ResponseEntity<Supplier> create(@Valid @RequestBody Supplier supplier) {
        return ResponseEntity.ok(supplierService.create(supplier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> update(@PathVariable Long id, @Valid @RequestBody Supplier supplier) {
        return ResponseEntity.ok(supplierService.update(id, supplier));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        supplierService.activate(id);
        return ResponseEntity.noContent().build();
    }
}