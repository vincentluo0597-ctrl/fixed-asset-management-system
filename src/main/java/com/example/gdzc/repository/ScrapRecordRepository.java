package com.example.gdzc.repository;

import com.example.gdzc.domain.ScrapRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapRecordRepository extends JpaRepository<ScrapRecord, Long> {
    List<ScrapRecord> findByEquipmentIdOrderByScrappedAtDesc(Long equipmentId);
}