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
}