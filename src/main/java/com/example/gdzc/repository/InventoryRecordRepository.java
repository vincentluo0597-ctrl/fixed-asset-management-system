package com.example.gdzc.repository;

import com.example.gdzc.domain.InventoryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRecordRepository extends JpaRepository<InventoryRecord, Long> {
    List<InventoryRecord> findByEquipmentId(Long equipmentId);
    Page<InventoryRecord> findByEquipmentId(Long equipmentId, Pageable pageable);
}