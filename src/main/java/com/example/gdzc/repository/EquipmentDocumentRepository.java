package com.example.gdzc.repository;

import com.example.gdzc.domain.EquipmentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EquipmentDocumentRepository extends JpaRepository<EquipmentDocument, Long>, JpaSpecificationExecutor<EquipmentDocument> {
    List<EquipmentDocument> findByEquipmentId(Long equipmentId);
}