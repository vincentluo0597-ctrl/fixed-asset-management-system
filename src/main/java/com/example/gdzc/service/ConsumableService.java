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
    /**
     * 耗材领域服务：支持分页筛选（关键字/低库存）、更换登记与简单库存统计。
     */
    private final ConsumableRepository consumableRepository;
    private final ConsumableReplacementRecordRepository replacementRecordRepository;
    private final OperationLogService operationLogService;

    public Page<Consumable> page(String keyword, Boolean lowStockOnly, Pageable pageable) {
        // 组合条件：名称/编码模糊，低库存（总库存 < 安全库存）
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
        // 登记更换记录，并在寿命为天时计算下一到期时间；写入操作日志
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

    public java.util.List<Consumable> listAll() {
        return consumableRepository.findAll();
    }

    public ConsumableStats stats() {
        // 简单统计：总数、缺货（<安全）、预警（<安全*1.2）、安全；并给出 Top10 低库存列表
        java.util.List<Consumable> all = consumableRepository.findAll();
        int total = all.size();
        int low = 0, warn = 0;
        java.util.List<ConsumableStats.Item> topLow = new java.util.ArrayList<>();
        java.util.List<Consumable> sorted = new java.util.ArrayList<>(all);
        sorted.sort((a,b) -> Integer.compare((a.getTotalStock()==null?0:a.getTotalStock()) - (a.getSafetyStock()==null?0:a.getSafetyStock()),
                                             (b.getTotalStock()==null?0:b.getTotalStock()) - (b.getSafetyStock()==null?0:b.getSafetyStock())));
        for (Consumable c : all) {
            int totalStock = c.getTotalStock()==null?0:c.getTotalStock();
            int safety = c.getSafetyStock()==null?0:c.getSafetyStock();
            if (safety>0 && totalStock < safety) low++; else if (safety>0 && totalStock < Math.round(safety*1.2)) warn++;
        }
        for (int i=0; i<Math.min(10, sorted.size()); i++) {
            Consumable c = sorted.get(i);
            topLow.add(new ConsumableStats.Item(c.getId(), c.getCode(), c.getName(), c.getTotalStock(), c.getSafetyStock()));
        }
        int safe = total - low - warn;
        return new ConsumableStats(total, low, warn, safe, topLow);
    }

    public record ConsumableStats(int total, int low, int warn, int safe, java.util.List<Item> topLow) {
        public record Item(Long id, String code, String name, Integer totalStock, Integer safetyStock) {}
    }
}
