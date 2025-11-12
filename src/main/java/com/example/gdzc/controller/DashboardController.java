package com.example.gdzc.controller;

import com.example.gdzc.domain.Equipment;
import com.example.gdzc.domain.EquipmentStatus;
import com.example.gdzc.domain.OperationLog;
import com.example.gdzc.repository.EquipmentRepository;
import com.example.gdzc.repository.OperationLogRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    /**
     * 仪表盘指标：设备总数、按状态分布、保修到期 Top10、近7天操作日志趋势。
     * 前端首页用于概览资产状态与运维活跃度。
     */
    private final EquipmentRepository equipmentRepository;
    private final OperationLogRepository operationLogRepository;

    @GetMapping("/metrics")
    public ResponseEntity<Metrics> metrics() {
        long equipmentTotal = equipmentRepository.count();
        Map<EquipmentStatus, Long> statusMap = new EnumMap<>(EquipmentStatus.class);
        for (EquipmentStatus s : EquipmentStatus.values()) {
            statusMap.put(s, equipmentRepository.countByStatus(s));
        }

        List<Equipment> expiring = equipmentRepository.findExpiringWarranties(LocalDate.now().plusDays(30));
        List<WarrantyItem> warrantyTop = expiring.stream()
                .sorted((a, b) -> {
                    LocalDate ad = a.getWarrantyExpiryDate();
                    LocalDate bd = b.getWarrantyExpiryDate();
                    if (ad == null && bd == null) return 0;
                    if (ad == null) return 1;
                    if (bd == null) return -1;
                    return ad.compareTo(bd);
                })
                .limit(10)
                .map(e -> new WarrantyItem(e.getId(), e.getName(), e.getWarrantyExpiryDate(), daysLeft(e.getWarrantyExpiryDate())))
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        LocalDateTime from = today.minusDays(6).atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        List<OperationLog> logs = operationLogRepository.findAll((root, q, cb) -> cb.between(root.get("createdAt"), from, to));
        Map<LocalDate, Long> perDay = logs.stream()
                .collect(Collectors.groupingBy(l -> l.getCreatedAt().toLocalDate(), Collectors.counting()));
        List<DayCount> last7 = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            last7.add(new DayCount(d, perDay.getOrDefault(d, 0L)));
        }

        Metrics m = new Metrics(equipmentTotal, toStatusEntries(statusMap), warrantyTop, last7);
        return ResponseEntity.ok(m);
    }

    private List<StatusEntry> toStatusEntries(Map<EquipmentStatus, Long> map) {
        List<StatusEntry> list = new ArrayList<>();
        for (Map.Entry<EquipmentStatus, Long> e : map.entrySet()) {
            list.add(new StatusEntry(e.getKey().name(), e.getValue()));
        }
        return list;
    }

    private Integer daysLeft(LocalDate date) {
        if (date == null) return null;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    @Data
    @AllArgsConstructor
    public static class Metrics {
        private long equipmentTotal;
        private List<StatusEntry> byStatus;
        private List<WarrantyItem> warrantyExpiringTop;
        private List<DayCount> operationsLast7Days;
    }

    @Data
    @AllArgsConstructor
    public static class StatusEntry { private String status; private long count; }

    @Data
    @AllArgsConstructor
    public static class WarrantyItem { private Long id; private String name; private LocalDate warrantyExpiryDate; private Integer daysLeft; }

    @Data
    @AllArgsConstructor
    public static class DayCount { private LocalDate date; private long count; }
}
