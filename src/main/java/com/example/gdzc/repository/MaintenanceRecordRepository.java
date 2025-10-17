package com.example.gdzc.repository;

import com.example.gdzc.domain.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long>, JpaSpecificationExecutor<MaintenanceRecord> {
    Optional<MaintenanceRecord> findTopByEquipmentIdAndEndAtIsNullOrderByStartAtDesc(Long equipmentId);
    List<MaintenanceRecord> findByEquipmentIdOrderByStartAtDesc(Long equipmentId);

    long countByEndAtIsNull();
    long countByEndAtIsNotNullAndEndAtBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = "select avg(timestampdiff(hour, start_at, end_at)) from maintenance_records where end_at is not null", nativeQuery = true)
    Double averageDurationHours();
}