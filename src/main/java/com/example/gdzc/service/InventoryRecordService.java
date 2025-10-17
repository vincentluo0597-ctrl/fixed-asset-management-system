package com.example.gdzc.service;

import com.example.gdzc.domain.InventoryRecord;
import com.example.gdzc.domain.Equipment;
import com.example.gdzc.dto.InventoryRecordDTO;
import com.example.gdzc.repository.InventoryRecordRepository;
import com.example.gdzc.repository.EquipmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryRecordService {
    private final InventoryRecordRepository inventoryRecordRepository;
    private final EquipmentRepository equipmentRepository;
    private final OperationLogService operationLogService;

    public List<InventoryRecord> listByEquipmentId(Long equipmentId) {
        if (equipmentId == null) {
            // 若未传设备ID，则返回全部（不建议大量数据时使用）
            return inventoryRecordRepository.findAll();
        }
        return inventoryRecordRepository.findByEquipmentId(equipmentId);
    }

    /**
     * 按设备ID查询最近的盘库记录，按创建时间倒序。
     */
    public List<InventoryRecord> listByEquipmentId(Long equipmentId, Integer limit) {
        if (equipmentId == null) {
            return inventoryRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        int size = (limit == null || limit <= 0) ? 10 : Math.min(limit, 100);
        return inventoryRecordRepository
                .findByEquipmentId(equipmentId, PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
    }

    @Transactional
    public InventoryRecord create(Long equipmentId, InventoryRecordDTO dto) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        // 处理数量调整逻辑
        boolean doAdjust = Boolean.TRUE.equals(dto.getAdjustQuantity()) || dto.getNewQuantity() != null;
        Integer newQty = dto.getNewQuantity();
        String adjustReason = dto.getAdjustReason();
        if (doAdjust) {
            if (newQty == null) {
                throw new IllegalArgumentException("选择调整数量时，必须提供调整后的数量");
            }
            if (newQty < 0) {
                throw new IllegalArgumentException("调整后的数量不能为负数");
            }
            if (adjustReason == null || adjustReason.isBlank()) {
                throw new IllegalArgumentException("请填写数量调整的变更原因");
            }
        }

        InventoryRecord record = InventoryRecord.builder()
                .equipmentId(equipment.getId())
                .source(dto.getSource())
                .note(dto.getNote())
                .adjustAfterQuantity(doAdjust ? newQty : null)
                .adjustReason(doAdjust ? adjustReason : null)
                .build();
        InventoryRecord saved = inventoryRecordRepository.save(record);
        // 如进行了数量调整，同步更新设备数量
        if (doAdjust) {
            Integer oldQty = equipment.getQuantity();
            equipment.setQuantity(newQty);
            equipmentRepository.save(equipment);
            String details = String.format("盘库记录（数量调整）：%d -> %d，原因=%s；source=%s, note=%s",
                    oldQty == null ? 0 : oldQty, newQty, adjustReason, dto.getSource(), dto.getNote());
            operationLogService.logWithCurrentUser("Equipment", equipment.getId(), "INVENTORY", details);
        } else {
            // 普通盘库日志
            String details = String.format("盘库记录：source=%s, note=%s", dto.getSource(), dto.getNote());
            operationLogService.logWithCurrentUser("Equipment", equipment.getId(), "INVENTORY", details);
        }
        // 同时在盘库记录实体上记录创建动作（便于按记录维度查询操作者）
        operationLogService.logWithCurrentUser("InventoryRecord", saved.getId(), "CREATE", "创建盘库记录");
        return saved;
    }
}