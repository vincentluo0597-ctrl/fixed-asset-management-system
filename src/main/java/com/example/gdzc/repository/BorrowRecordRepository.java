package com.example.gdzc.repository;

import com.example.gdzc.domain.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {
    Optional<BorrowRecord> findTopByEquipmentIdAndReturnDateIsNullOrderByBorrowDateDesc(Long equipmentId);
    List<BorrowRecord> findByEquipmentIdOrderByBorrowDateDesc(Long equipmentId);

    long countByReturnDateIsNull();
    long countByReturnDateIsNullAndExpectedReturnDateBefore(LocalDate date);
    long countByReturnDateIsNotNullAndReturnDateBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = "select avg(timestampdiff(hour, borrow_date, return_date)) from borrow_records where return_date is not null", nativeQuery = true)
    Double averageDurationHours();
}