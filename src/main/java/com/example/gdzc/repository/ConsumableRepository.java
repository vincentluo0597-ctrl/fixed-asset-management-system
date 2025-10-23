package com.example.gdzc.repository;

import com.example.gdzc.domain.Consumable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ConsumableRepository extends JpaRepository<Consumable, Long>, JpaSpecificationExecutor<Consumable> {
    Optional<Consumable> findByCode(String code);
}