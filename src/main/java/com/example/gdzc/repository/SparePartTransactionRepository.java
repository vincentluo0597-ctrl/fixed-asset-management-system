package com.example.gdzc.repository;

import com.example.gdzc.domain.SparePartTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SparePartTransactionRepository extends JpaRepository<SparePartTransaction, Long> {
    Page<SparePartTransaction> findByPartId(Long partId, Pageable pageable);
}