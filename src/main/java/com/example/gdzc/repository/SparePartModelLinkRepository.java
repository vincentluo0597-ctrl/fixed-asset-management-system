package com.example.gdzc.repository;

import com.example.gdzc.domain.SparePartModelLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SparePartModelLinkRepository extends JpaRepository<SparePartModelLink, Long> {
    List<SparePartModelLink> findByEquipmentModel(String equipmentModel);
    List<SparePartModelLink> findByPartId(Long partId);
}