package com.example.gdzc.controller;

import com.example.gdzc.domain.Equipment;
import com.example.gdzc.domain.EquipmentStatus;
import com.example.gdzc.dto.EquipmentDTO;
import com.example.gdzc.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {
    private final EquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<List<Equipment>> list() {
        return ResponseEntity.ok(equipmentService.list());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Equipment>> page(Pageable pageable) {
        return ResponseEntity.ok(equipmentService.page(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getById(@PathVariable Long id) {
        return equipmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 组合筛选与搜索分页：支持 status、categoryId、supplierId、locationId、keeperId、keyword（模糊匹配）
     * 使用 Spring Data JPA Specification 构建动态条件
     */
    @GetMapping("/query")
    public ResponseEntity<Page<Equipment>> query(
            @RequestParam(required = false) EquipmentStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long keeperId,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Specification<Equipment> spec = Specification.where(null);
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoryId"), categoryId));
        }
        if (supplierId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("supplierId"), supplierId));
        }
        if (locationId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("locationId"), locationId));
        }
        if (keeperId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("keeperId"), keeperId));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(root.get("name"), like),
                    cb.like(root.get("model"), like),
                    cb.like(root.get("brand"), like),
                    cb.like(root.get("serialNumber"), like),
                    cb.like(root.get("assetNumber"), like)
            ));
        }
        return ResponseEntity.ok(equipmentService.page(spec, pageable));
    }

    /**
     * 导出设备列表为 CSV，支持与 /query 相同的筛选条件。
     * 输出采用 UTF-8 带 BOM，兼容 Excel。文件名包含导出时间。
     */
    @GetMapping(value = "/export")
    public ResponseEntity<StreamingResponseBody> export(
            @RequestParam(required = false) EquipmentStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long keeperId,
            @RequestParam(required = false) String keyword
    ) {
        Specification<Equipment> spec = Specification.where(null);
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoryId"), categoryId));
        }
        if (supplierId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("supplierId"), supplierId));
        }
        if (locationId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("locationId"), locationId));
        }
        if (keeperId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("keeperId"), keeperId));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(root.get("name"), like),
                    cb.like(root.get("model"), like),
                    cb.like(root.get("brand"), like),
                    cb.like(root.get("serialNumber"), like),
                    cb.like(root.get("assetNumber"), like)
            ));
        }

        final List<Equipment> list = equipmentService.list(spec);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE + "; charset=UTF-8");
        String filename = "inventory_" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        StreamingResponseBody body = outputStream -> {
            // UTF-8 BOM
            outputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
            // Header
            String header = String.join(",",
                    "ID","名称","状态","品牌","型号","数量",
                    "分类ID","供应商ID","位置ID","保管人ID",
                    "采购日期","资产编号","序列号"
            ) + "\n";
            outputStream.write(header.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // Rows
            for (Equipment e : list) {
                String line = String.join(",",
                        csv(e.getId()),
                        csv(e.getName()),
                        csv(e.getStatus() != null ? e.getStatus().name() : null),
                        csv(e.getBrand()),
                        csv(e.getModel()),
                        csv(e.getQuantity()),
                        csv(e.getCategoryId()),
                        csv(e.getSupplierId()),
                        csv(e.getLocationId()),
                        csv(e.getKeeperId()),
                        csv(e.getPurchaseDate()),
                        csv(e.getAssetNumber()),
                        csv(e.getSerialNumber())
                ) + "\n";
                outputStream.write(line.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            outputStream.flush();
        };

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    // CSV 字段包装：用双引号包裹，并转义内部双引号；空值返回空字符串
    private String csv(Object v) {
        String s = (v == null) ? "" : String.valueOf(v);
        s = s.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    @PostMapping
    public ResponseEntity<Equipment> create(@Valid @RequestBody EquipmentDTO dto) {
        return ResponseEntity.ok(equipmentService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipment> update(@PathVariable Long id, @Valid @RequestBody EquipmentDTO dto) {
        return ResponseEntity.ok(equipmentService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Equipment>> findByStatus(@PathVariable("status") EquipmentStatus status) {
        return ResponseEntity.ok(equipmentService.findByStatus(status));
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<Equipment>> findByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(equipmentService.findByLocationId(locationId));
    }

    @GetMapping("/keeper/{keeperId}")
    public ResponseEntity<List<Equipment>> findByKeeper(@PathVariable Long keeperId) {
        return ResponseEntity.ok(equipmentService.findByKeeperId(keeperId));
    }
}