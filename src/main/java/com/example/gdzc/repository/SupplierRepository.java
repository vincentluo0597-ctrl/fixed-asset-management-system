package com.example.gdzc.repository;

import com.example.gdzc.domain.Supplier;
import com.example.gdzc.domain.SupplierType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByCode(String code);

    List<Supplier> findByIsActiveTrue();

    List<Supplier> findByTypeAndIsActiveTrue(SupplierType type);

    @Query("SELECT s FROM Supplier s WHERE s.name LIKE %:keyword% OR s.code LIKE %:keyword%")
    Page<Supplier> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByCode(String code);
}