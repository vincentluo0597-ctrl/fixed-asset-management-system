package com.example.gdzc.service;

import com.example.gdzc.domain.Consumable;
import com.example.gdzc.domain.ConsumableReplacementRecord;
import com.example.gdzc.repository.ConsumableReplacementRecordRepository;
import com.example.gdzc.repository.ConsumableRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConsumableService {
    private final ConsumableRepository consumableRepository;
    private final ConsumableReplacementRecordRepository replacementRecordRepository;
    private final OperationLogService operationLogService;

    public Page<Consumable> page(String keyword, Boolean lowStockOnly, Pageable pageable) {
        Specification<Consumable> spec = Specification.where(null);
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), like),
                    cb.like(cb.lower(root.get("name")), like)
            ));
        }
        if (Boolean.TRUE.equals(lowStockOnly)) {
            spec = spec.and((root, q, cb) -> cb.lessThan(root.get("totalStock"), root.get("safetyStock")));
        }
        Pageable effective = pageable == null ? PageRequest.of(0, 20) : pageable;
        return consumableRepository.findAll(spec, effective);
    }

    @Transactional
    public ConsumableReplacementRecord recordReplacement(Long consumableId, Long equipmentId, Integer usageValue, String note) {
        ConsumableReplacementRecord rec = ConsumableReplacementRecord.builder()
                .consumableId(consumableId)
                .equipmentId(equipmentId)
                .usageValue(usageValue)
                .note(note)
                .replacedAt(LocalDateTime.now())
                .build();
        // 根据寿命类型计算下次到期时间（若为 DAYS）
        Consumable c = consumableRepository.findById(consumableId).orElseThrow();
        if (c.getLifeType() == Consumable.LifeType.DAYS && c.getLifeValue() != null) {
            rec.setNextDueAt(LocalDateTime.now().plusDays(c.getLifeValue()));
        }
        ConsumableReplacementRecord saved = replacementRecordRepository.save(rec);
        operationLogService.logWithCurrentUser("Consumable", consumableId, "REPLACEMENT",
                "耗材更换记录：equipment=" + equipmentId + ", usageValue=" + usageValue);
        return saved;
    }
}