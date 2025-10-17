package com.example.gdzc.repository;

import com.example.gdzc.domain.EquipmentCategory;
import com.example.gdzc.domain.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, Long> {
    
    Optional<EquipmentCategory> findByCode(String code);
    
    List<EquipmentCategory> findByParentId(Long parentId);
    
    List<EquipmentCategory> findByIsActiveTrue();

    List<EquipmentCategory> findByParentIdIsNullAndIsActiveTrueOrderBySortOrder();

    List<EquipmentCategory> findByParentIdAndIsActiveTrueOrderBySortOrder(Long parentId);

    List<EquipmentCategory> findByTypeAndIsActiveTrue(EquipmentType type);
    
    @Query("SELECT ec FROM EquipmentCategory ec WHERE ec.parentId IS NULL AND ec.isActive = true ORDER BY ec.sortOrder")
    List<EquipmentCategory> findRootCategories();
    
    boolean existsByCode(String code);
}