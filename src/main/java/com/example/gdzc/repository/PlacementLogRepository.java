package com.example.gdzc.repository;

import com.example.gdzc.domain.PlacementLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacementLogRepository extends JpaRepository<PlacementLog, Long> {
    List<PlacementLog> findByEquipmentIdOrderByMovedAtDesc(Long equipmentId);
}