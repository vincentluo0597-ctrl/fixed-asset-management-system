package com.example.gdzc.service;

import com.example.gdzc.domain.MaintenanceRecord;
import com.example.gdzc.domain.SparePart;
import com.example.gdzc.domain.SparePartInventory;
import com.example.gdzc.domain.SparePartTransaction;
import com.example.gdzc.dto.MaintenanceCreateWithPartsDTO;
import com.example.gdzc.dto.UsedSparePartDTO;
import com.example.gdzc.repository.MaintenanceRecordRepository;
import com.example.gdzc.repository.SparePartInventoryRepository;
import com.example.gdzc.repository.SparePartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MaintenanceService {
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final OperationLogService operationLogService;
    private final SparePartService sparePartService;
    private final SparePartRepository sparePartRepository;
    private final SparePartInventoryRepository inventoryRepository;

    public Page<MaintenanceRecord> page(Specification<MaintenanceRecord> spec, Pageable pageable) {
        return maintenanceRecordRepository.findAll(spec, pageable);
    }

    public long countOngoing() {
        return maintenanceRecordRepository.countByEndAtIsNull();
    }

    public long countCompletedBetween(LocalDateTime start, LocalDateTime end) {
        return maintenanceRecordRepository.countByEndAtIsNotNullAndEndAtBetween(start, end);
    }

    public Double averageDurationHours() {
        return maintenanceRecordRepository.averageDurationHours();
    }

    @Transactional
    public MaintenanceRecord create(MaintenanceRecord record) {
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        operationLogService.logWithCurrentUser("MaintenanceRecord", saved.getId(), "CREATE", "新增维修记录");
        return saved;
    }

    /**
     * 创建维修记录并在需要时实时扣减备件库存。
     * 规则：
     * - 若 deductStock = true，则逐条检查库存；不足时抛出异常阻止创建，以避免维修延误。
     * - locationId 未提供时，优先使用备件的 defaultLocationId；若仍为空且需要扣减，则抛出异常。
     * - 扣减成功会记录 SparePartTransaction，并在安全库存下发出预警日志。
     * 返回：包含 createdRecord 与 operations（每个行项目的处理结果）。
     */
    @Transactional
    public Map<String, Object> createWithSpareParts(MaintenanceCreateWithPartsDTO dto) {
        if (dto.getEquipmentId() == null) throw new IllegalArgumentException("设备ID不能为空");
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) throw new IllegalArgumentException("维修描述不能为空");
        boolean deduct = dto.getDeductStock() == null ? true : dto.getDeductStock();

        MaintenanceRecord toSave = MaintenanceRecord.builder()
                .equipmentId(dto.getEquipmentId())
                .description(dto.getDescription().trim())
                .startAt(LocalDateTime.now())
                .build();
        MaintenanceRecord created = maintenanceRecordRepository.save(toSave);
        operationLogService.logWithCurrentUser("MaintenanceRecord", created.getId(), "CREATE", "新增维修记录（含备件联动）");

        List<Map<String, Object>> operations = new ArrayList<>();
        List<UsedSparePartDTO> lines = dto.getUsedParts() != null ? dto.getUsedParts() : Collections.emptyList();

        if (!lines.isEmpty()) {
            for (UsedSparePartDTO line : lines) {
                Long partId = line.getPartId();
                Integer qty = line.getQuantity();
                if (partId == null || qty == null || qty <= 0) {
                    throw new IllegalArgumentException("备件行项目参数不完整：partId 与 quantity 必须填写，数量需为正数");
                }
                SparePart part = sparePartRepository.findById(partId)
                        .orElseThrow(() -> new IllegalArgumentException("备件不存在：ID=" + partId));
                Long locationId = (line.getLocationId() != null) ? line.getLocationId() : part.getDefaultLocationId();
                if (deduct && locationId == null) {
                    throw new IllegalArgumentException("备件 " + part.getCode() + " 未设置默认库位，且未在请求中指定库位，无法扣减库存");
                }

                Map<String, Object> op = new LinkedHashMap<>();
                op.put("partId", partId);
                op.put("partCode", part.getCode());
                op.put("locationId", locationId);
                op.put("requestedQty", qty);

                if (deduct) {
                    int available = inventoryRepository.findByPartIdAndLocationId(partId, locationId)
                            .map(SparePartInventory::getQuantity).orElse(0);
                    if (available < qty) {
                        throw new IllegalArgumentException("备件库存不足：" + part.getCode() + " 位置=" + locationId + " 可用=" + available + "，需要=" + qty);
                    }
                    sparePartService.adjustStock(partId, locationId, qty, SparePartTransaction.TransactionType.OUT,
                            "维修工单领用，工单ID=" + created.getId(), created.getId());
                    op.put("deducted", true);
                    op.put("message", "已领用并扣减库存");
                    operationLogService.logWithCurrentUser("MaintenanceRecord", created.getId(), "USE_SPARE_PART",
                            "领用备件 " + part.getCode() + " 数量=" + qty + "，库位=" + locationId);
                } else {
                    op.put("deducted", false);
                    op.put("message", "未执行扣减，仅关联备件");
                }
                operations.add(op);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("record", created);
        result.put("operations", operations);
        return result;
    }
}