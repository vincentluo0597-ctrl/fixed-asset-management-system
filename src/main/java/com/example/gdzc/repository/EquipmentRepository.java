package com.example.gdzc.repository;

import com.example.gdzc.domain.Equipment;
import com.example.gdzc.domain.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {
    
    List<Equipment> findByStatus(EquipmentStatus status);
    
    List<Equipment> findByLocationId(Long locationId);
    
    List<Equipment> findByKeeperId(Long keeperId);
    
    List<Equipment> findByCategoryId(Long categoryId);
    
    List<Equipment> findBySupplierId(Long supplierId);
    
    Optional<Equipment> findByAssetNumber(String assetNumber);
    
    boolean existsByAssetNumber(String assetNumber);
    
    @Query("SELECT e FROM Equipment e WHERE e.name LIKE %:keyword% OR e.model LIKE %:keyword% OR e.brand LIKE %:keyword% OR e.serialNumber LIKE %:keyword% OR e.assetNumber LIKE %:keyword%")
    List<Equipment> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT e FROM Equipment e WHERE e.status = :status AND e.locationId = :locationId")
    List<Equipment> findByStatusAndLocationId(@Param("status") EquipmentStatus status, @Param("locationId") Long locationId);
    
    @Query("SELECT e FROM Equipment e WHERE e.warrantyExpiryDate <= :date AND e.warrantyExpiryDate IS NOT NULL")
    List<Equipment> findExpiringWarranties(@Param("date") java.time.LocalDate date);
    
    long countByStatus(EquipmentStatus status);
    
    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.categoryId = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.supplierId = :supplierId")
    long countBySupplierId(@Param("supplierId") Long supplierId);
}