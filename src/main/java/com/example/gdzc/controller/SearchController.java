package com.example.gdzc.controller;

import com.example.gdzc.domain.Equipment;
import com.example.gdzc.domain.Supplier;
import com.example.gdzc.domain.Location;
import com.example.gdzc.repository.EquipmentRepository;
import com.example.gdzc.repository.SupplierRepository;
import com.example.gdzc.repository.LocationRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    /**
     * 全局搜索控制器：同时检索设备/供应商/位置的关键字匹配，返回总数与 TopN 概览。
     * 供仪表盘快速定位与跳转使用。
     */
    private final EquipmentRepository equipmentRepository;
    private final SupplierRepository supplierRepository;
    private final LocationRepository locationRepository;

    @GetMapping
    public ResponseEntity<SearchResponse> search(@RequestParam("q") String q,
                                                 @RequestParam(value = "limit", required = false) Integer limit) {
        String keyword = q == null ? "" : q.trim();
        int top = (limit == null || limit <= 0) ? 5 : Math.min(limit, 20);

        Specification<Equipment> equipSpec = Specification.where(null);
        if (!keyword.isEmpty()) {
            String like = "%" + keyword + "%";
            equipSpec = equipSpec.and((root, query, cb) -> cb.or(
                    cb.like(root.get("name"), like),
                    cb.like(root.get("model"), like),
                    cb.like(root.get("brand"), like),
                    cb.like(root.get("serialNumber"), like),
                    cb.like(root.get("assetNumber"), like)
            ));
        }
        List<Equipment> equipAll = equipmentRepository.findAll(equipSpec);
        List<EquipmentItem> equipTop = equipAll.stream().limit(top)
                .map(e -> new EquipmentItem(e.getId(), e.getName(), e.getStatus() != null ? e.getStatus().name() : null, e.getModel(), e.getBrand()))
                .collect(Collectors.toList());

        List<Supplier> supAll;
        if (!keyword.isEmpty()) {
            supAll = supplierRepository.searchByKeyword(keyword, org.springframework.data.domain.PageRequest.of(0, 200)).getContent();
        } else {
            supAll = supplierRepository.findAll();
        }
        List<SupplierItem> supTop = supAll.stream().limit(top)
                .map(s -> new SupplierItem(s.getId(), s.getName()))
                .collect(Collectors.toList());

        List<Location> locAll;
        if (!keyword.isEmpty()) {
            locAll = locationRepository.findByNameContainingIgnoreCase(keyword);
        } else {
            locAll = locationRepository.findAll();
        }
        List<LocationItem> locTop = locAll.stream().limit(top)
                .map(l -> new LocationItem(l.getId(), l.getName()))
                .collect(Collectors.toList());

        SearchResponse resp = new SearchResponse(keyword, equipAll.size(), supAll.size(), locAll.size(), equipTop, supTop, locTop);
        return ResponseEntity.ok(resp);
    }

    @Data
    @AllArgsConstructor
    public static class SearchResponse {
        private String keyword;
        private int equipmentCount;
        private int supplierCount;
        private int locationCount;
        private List<EquipmentItem> equipmentTop;
        private List<SupplierItem> supplierTop;
        private List<LocationItem> locationTop;
    }

    @Data
    @AllArgsConstructor
    public static class EquipmentItem {
        private Long id;
        private String name;
        private String status;
        private String model;
        private String brand;
    }

    @Data
    @AllArgsConstructor
    public static class SupplierItem {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class LocationItem {
        private Long id;
        private String name;
    }
}
