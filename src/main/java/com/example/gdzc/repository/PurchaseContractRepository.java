package com.example.gdzc.repository;

import com.example.gdzc.domain.ContractStatus;
import com.example.gdzc.domain.PurchaseContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseContractRepository extends JpaRepository<PurchaseContract, Long> {

    Optional<PurchaseContract> findByContractNumber(String contractNumber);

    List<PurchaseContract> findBySupplierId(Long supplierId);

    List<PurchaseContract> findByStatus(ContractStatus status);

    List<PurchaseContract> findByStatusNot(ContractStatus status);

    @Query("SELECT pc FROM PurchaseContract pc WHERE pc.contractNumber LIKE %:keyword% OR pc.contractName LIKE %:keyword%")
    Page<PurchaseContract> findByKeywordContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByContractNumber(String contractNumber);

    long countByStatus(ContractStatus status);

    long countBySupplierId(Long supplierId);

    List<PurchaseContract> findByEndDateLessThanEqual(LocalDate date);
}