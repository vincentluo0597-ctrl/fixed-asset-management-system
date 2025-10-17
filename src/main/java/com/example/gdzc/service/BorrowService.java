package com.example.gdzc.service;

import com.example.gdzc.domain.BorrowRecord;
import com.example.gdzc.repository.BorrowRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BorrowService {
    private final BorrowRecordRepository borrowRecordRepository;
    private final OperationLogService operationLogService;

    public Page<BorrowRecord> page(Specification<BorrowRecord> spec, Pageable pageable) {
        return borrowRecordRepository.findAll(spec, pageable);
    }

    public long countOngoing() {
        return borrowRecordRepository.countByReturnDateIsNull();
    }

    public long countOverdue(LocalDate today) {
        return borrowRecordRepository.countByReturnDateIsNullAndExpectedReturnDateBefore(today);
    }

    public long countReturnedBetween(LocalDateTime start, LocalDateTime end) {
        return borrowRecordRepository.countByReturnDateIsNotNullAndReturnDateBetween(start, end);
    }

    public Double averageDurationHours() {
        return borrowRecordRepository.averageDurationHours();
    }

    @Transactional
    public BorrowRecord create(BorrowRecord record) {
        BorrowRecord saved = borrowRecordRepository.save(record);
        operationLogService.logWithCurrentUser("BorrowRecord", saved.getId(), "CREATE", "新增借用记录");
        return saved;
    }
}