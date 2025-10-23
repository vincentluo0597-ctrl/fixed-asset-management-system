package com.example.gdzc.repository;

import com.example.gdzc.domain.ConsumableReplacementRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumableReplacementRecordRepository extends JpaRepository<ConsumableReplacementRecord, Long> {
    Page<ConsumableReplacementRecord> findByConsumableId(Long consumableId, Pageable pageable);
    Page<ConsumableReplacementRecord> findByEquipmentId(Long equipmentId, Pageable pageable);
}