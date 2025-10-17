package com.example.gdzc.service;

import com.example.gdzc.domain.MaintenanceRecord;
import com.example.gdzc.repository.MaintenanceRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MaintenanceService {
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final OperationLogService operationLogService;

    public Page<MaintenanceRecord> page(Specification<MaintenanceRecord> spec, Pageable pageable) {
        return maintenanceRecordRepository.findAll(spec, pageable);
    }

    public long countOngoing() {
        return maintenanceRecordRepository.countByEndAtIsNull();
    }

    public long countCompletedBetween(LocalDateTime start, LocalDateTime end) {
        return maintenanceRecordRepository.countByEndAtIsNotNullAndEndAtBetween(start, end);
    }

    public Double averageDurationHours() {
        return maintenanceRecordRepository.averageDurationHours();
    }

    @Transactional
    public MaintenanceRecord create(MaintenanceRecord record) {
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        operationLogService.logWithCurrentUser("MaintenanceRecord", saved.getId(), "CREATE", "新增维修记录");
        return saved;
    }
}