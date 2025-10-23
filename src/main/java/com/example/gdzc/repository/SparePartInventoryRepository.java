package com.example.gdzc.repository;

import com.example.gdzc.domain.SparePartInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SparePartInventoryRepository extends JpaRepository<SparePartInventory, Long> {
    List<SparePartInventory> findByPartId(Long partId);
    Optional<SparePartInventory> findByPartIdAndLocationId(Long partId, Long locationId);
}