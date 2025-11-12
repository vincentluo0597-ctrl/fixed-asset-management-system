package com.example.gdzc.service;

import com.example.gdzc.domain.SparePart;
import com.example.gdzc.domain.SparePartInventory;
import com.example.gdzc.domain.SparePartModelLink;
import com.example.gdzc.domain.SparePartTransaction;
import com.example.gdzc.repository.SparePartInventoryRepository;
import com.example.gdzc.repository.SparePartModelLinkRepository;
import com.example.gdzc.repository.SparePartRepository;
import com.example.gdzc.repository.SparePartTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SparePartService {
    /**
     * 备件领域服务：分页筛选、按设备型号映射、分库位库存与总库存维护、预警日志与统计。
     */
    private final SparePartRepository sparePartRepository;
    private final SparePartInventoryRepository inventoryRepository;
    private final SparePartTransactionRepository transactionRepository;
    private final SparePartModelLinkRepository modelLinkRepository;
    private final OperationLogService operationLogService;

    public Page<SparePart> page(String keyword, Boolean lowStockOnly, Pageable pageable) {
        Specification<SparePart> spec = Specification.where(null);
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), like),
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("model")), like)
            ));
        }
        if (Boolean.TRUE.equals(lowStockOnly)) {
            spec = spec.and((root, q, cb) -> cb.lessThan(root.get("totalStock"), root.get("safetyStock")));
        }
        Pageable effective = pageable == null ? PageRequest.of(0, 20) : pageable;
        return sparePartRepository.findAll(spec, effective);
    }

    public List<SparePartModelLink> findLinksByModel(String equipmentModel) {
        return modelLinkRepository.findByEquipmentModel(equipmentModel);
    }

    @Transactional
    public SparePart adjustStock(Long partId, Long locationId, int quantity, SparePartTransaction.TransactionType type, String note, Long refMaintenanceId) {
        // 分库位变更 + 总库存同步；出库负数保护；触发低库存预警日志
        SparePart part = sparePartRepository.findById(partId).orElseThrow();
        // 更新分库位库存
        SparePartInventory inv = inventoryRepository.findByPartIdAndLocationId(partId, locationId)
                .orElse(SparePartInventory.builder().partId(partId).locationId(locationId).quantity(0).build());
        if (type == SparePartTransaction.TransactionType.IN || type == SparePartTransaction.TransactionType.ADJUST) {
            inv.setQuantity(inv.getQuantity() + quantity);
        } else if (type == SparePartTransaction.TransactionType.OUT) {
            inv.setQuantity(inv.getQuantity() - quantity);
            if (inv.getQuantity() < 0) inv.setQuantity(0);
        }
        inventoryRepository.save(inv);
        // 更新总库存
        int delta = (type == SparePartTransaction.TransactionType.OUT) ? -quantity : quantity;
        int newTotal = (part.getTotalStock() == null ? 0 : part.getTotalStock()) + delta;
        part.setTotalStock(Math.max(newTotal, 0));
        sparePartRepository.save(part);
        // 记录流水
        transactionRepository.save(SparePartTransaction.builder()
                .partId(partId)
                .type(type)
                .quantity(quantity)
                .locationId(locationId)
                .refMaintenanceId(refMaintenanceId)
                .note(note)
                .build());
        // 安全库存预警
        if (part.getSafetyStock() != null && part.getTotalStock() < part.getSafetyStock()) {
            operationLogService.logWithCurrentUser("SparePart", part.getId(), "LOW_STOCK_ALERT",
                    "备件库存低于安全库存：" + part.getCode() + "(" + part.getName() + ") 当前=" + part.getTotalStock() + ", 安全=" + part.getSafetyStock());
        }
        return part;
    }

    public java.util.List<SparePart> listAll() {
        return sparePartRepository.findAll();
    }

    public SparePartStats stats() {
        java.util.List<SparePart> all = sparePartRepository.findAll();
        int total = all.size();
        int low = 0, warn = 0;
        java.util.List<SparePartStats.Item> topLow = new java.util.ArrayList<>();
        java.util.List<SparePart> sorted = new java.util.ArrayList<>(all);
        sorted.sort((a,b) -> Integer.compare((a.getTotalStock()==null?0:a.getTotalStock()) - (a.getSafetyStock()==null?0:a.getSafetyStock()),
                                             (b.getTotalStock()==null?0:b.getTotalStock()) - (b.getSafetyStock()==null?0:b.getSafetyStock())));
        for (SparePart c : all) {
            int totalStock = c.getTotalStock()==null?0:c.getTotalStock();
            int safety = c.getSafetyStock()==null?0:c.getSafetyStock();
            if (safety>0 && totalStock < safety) low++; else if (safety>0 && totalStock < Math.round(safety*1.2)) warn++;
        }
        for (int i=0; i<Math.min(10, sorted.size()); i++) {
            SparePart c = sorted.get(i);
            topLow.add(new SparePartStats.Item(c.getId(), c.getCode(), c.getName(), c.getTotalStock(), c.getSafetyStock()));
        }
        int safe = total - low - warn;
        return new SparePartStats(total, low, warn, safe, topLow);
    }

    public record SparePartStats(int total, int low, int warn, int safe, java.util.List<Item> topLow) {
        public record Item(Long id, String code, String name, Integer totalStock, Integer safetyStock) {}
    }
}
