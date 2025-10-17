package com.example.gdzc.service;

import com.example.gdzc.domain.*;
import com.example.gdzc.dto.BorrowRequestDTO;
import com.example.gdzc.dto.MaintenanceCompleteDTO;
import com.example.gdzc.dto.MaintenanceStartDTO;
import com.example.gdzc.dto.MoveRequestDTO;
import com.example.gdzc.dto.ScrapRequestDTO;
import com.example.gdzc.repository.*;
import com.example.gdzc.repository.LocationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EquipmentWorkflowService {
    private final EquipmentRepository equipmentRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final PlacementLogRepository placementLogRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final ScrapRecordRepository scrapRecordRepository;
    private final OperationLogService operationLogService;
    private final LocationRepository locationRepository;

    @Transactional
    public Equipment borrow(Long equipmentId, BorrowRequestDTO dto) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + equipmentId));
        if (equipment.getStatus() == EquipmentStatus.报废) {
            throw new IllegalArgumentException("报废设备不可借用");
        }
        // 维修中的设备不可借用
        maintenanceRecordRepository.findTopByEquipmentIdAndEndAtIsNullOrderByStartAtDesc(equipmentId)
                .ifPresent(r -> { throw new IllegalArgumentException("设备当前处于维修中，不可借用"); });
        // 已借出的设备不可重复借用
        borrowRecordRepository.findTopByEquipmentIdAndReturnDateIsNullOrderByBorrowDateDesc(equipmentId)
                .ifPresent(r -> { throw new IllegalArgumentException("设备当前处于借用中"); });

        BorrowRecord record = BorrowRecord.builder()
                .equipmentId(equipmentId)
                .borrowerId(dto.getBorrowerId())
                .expectedReturnDate(dto.getExpectedReturnDate())
                .borrowDate(java.time.LocalDateTime.now())
                .build();
        BorrowRecord savedBorrow = borrowRecordRepository.save(record);

        equipment.setStatus(EquipmentStatus.借用中);
        equipment.setKeeperId(dto.getBorrowerId());
        Equipment saved = equipmentRepository.save(equipment);
        operationLogService.logWithCurrentUser("Equipment", saved.getId(), "BORROW", "借用设备，借用人=" + dto.getBorrowerId());
        operationLogService.logWithCurrentUser("BorrowRecord", savedBorrow.getId(), "CREATE", "新增借用记录，借用人=" + dto.getBorrowerId());
        return saved;
    }

    @Transactional
    public Equipment maintenanceStart(Long equipmentId, MaintenanceStartDTO dto) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + equipmentId));
        if (equipment.getStatus() == EquipmentStatus.报废) {
            throw new IllegalArgumentException("报废设备不可维修");
        }
        // 借用中的设备不可维修
        borrowRecordRepository.findTopByEquipmentIdAndReturnDateIsNullOrderByBorrowDateDesc(equipmentId)
                .ifPresent(r -> { throw new IllegalArgumentException("设备当前处于借用中，不可维修"); });
        // 已在维修中的设备不可重复开始维修
        maintenanceRecordRepository.findTopByEquipmentIdAndEndAtIsNullOrderByStartAtDesc(equipmentId)
                .ifPresent(r -> { throw new IllegalArgumentException("设备当前处于维修中"); });

        MaintenanceRecord record = MaintenanceRecord.builder()
                .equipmentId(equipmentId)
                .description(dto.getDescription())
                .startAt(java.time.LocalDateTime.now())
                .build();
        MaintenanceRecord savedRecordStart = maintenanceRecordRepository.save(record);

        equipment.setStatus(EquipmentStatus.维修中);
        Equipment saved = equipmentRepository.save(equipment);
        operationLogService.logWithCurrentUser("Equipment", saved.getId(), "MAINT_START", "开始维修: " + dto.getDescription());
        operationLogService.logWithCurrentUser("MaintenanceRecord", savedRecordStart.getId(), "START", "开始维修: " + dto.getDescription());
        return saved;
    }

    @Transactional
    public Equipment returnBack(Long equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + equipmentId));
        BorrowRecord active = borrowRecordRepository
                .findTopByEquipmentIdAndReturnDateIsNullOrderByBorrowDateDesc(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("没有找到未归还的借用记录"));
        active.setReturnDate(java.time.LocalDateTime.now());
        borrowRecordRepository.save(active);

        equipment.setStatus(EquipmentStatus.在用);
        equipment.setKeeperId(null);
        Equipment saved = equipmentRepository.save(equipment);
        operationLogService.logWithCurrentUser("Equipment", saved.getId(), "RETURN", "归还设备");
        return saved;
    }

    @Transactional
    public Equipment move(Long equipmentId, MoveRequestDTO dto) {
        // 验证目标位置存在
        locationRepository.findById(dto.getToLocationId())
                .orElseThrow(() -> new IllegalArgumentException("目标位置不存在: " + dto.getToLocationId()));
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + equipmentId));
        if (equipment.getStatus() == EquipmentStatus.报废) {
            throw new IllegalArgumentException("报废设备不可迁移");
        }
        Long fromLocation = equipment.getLocationId();
        PlacementLog log = PlacementLog.builder()
                .equipmentId(equipmentId)
                .fromLocationId(fromLocation)
                .toLocationId(dto.getToLocationId())
                .movedAt(java.time.LocalDateTime.now())
                .build();
        PlacementLog savedPlacement = placementLogRepository.save(log);

        equipment.setLocationId(dto.getToLocationId());
        Equipment saved = equipmentRepository.save(equipment);
        operationLogService.logWithCurrentUser("Equipment", saved.getId(), "MOVE", "设备位置从 " + fromLocation + " 迁移到 " + dto.getToLocationId());
        operationLogService.logWithCurrentUser("PlacementLog", savedPlacement.getId(), "CREATE", "位置迁移：from=" + fromLocation + ", to=" + dto.getToLocationId());
        return saved;
    }

    @Transactional
    public Equipment maintenanceComplete(Long equipmentId, MaintenanceCompleteDTO dto) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + equipmentId));
        MaintenanceRecord record = maintenanceRecordRepository
                .findTopByEquipmentIdAndEndAtIsNullOrderByStartAtDesc(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("没有进行中的维修记录"));
        record.setEndAt(LocalDateTime.now());
        record.setResult(dto.getResult());
        MaintenanceRecord savedRecordComplete = maintenanceRecordRepository.save(record);

        equipment.setStatus(EquipmentStatus.在用);
        Equipment saved = equipmentRepository.save(equipment);
        operationLogService.logWithCurrentUser("Equipment", saved.getId(), "MAINT_COMPLETE", "完成维修: " + dto.getResult());
        operationLogService.logWithCurrentUser("MaintenanceRecord", savedRecordComplete.getId(), "COMPLETE", "完成维修: " + dto.getResult());
        return saved;
    }

    @Transactional
    public Equipment scrap(Long equipmentId, ScrapRequestDTO dto) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + equipmentId));
        if (equipment.getStatus() == EquipmentStatus.报废) {
            throw new IllegalArgumentException("设备已是报废状态");
        }
        ScrapRecord record = ScrapRecord.builder()
                .equipmentId(equipmentId)
                .reason(dto.getReason())
                .scrappedAt(LocalDateTime.now())
                .build();
        ScrapRecord savedScrap = scrapRecordRepository.save(record);

        equipment.setStatus(EquipmentStatus.报废);
        Equipment saved = equipmentRepository.save(equipment);
        operationLogService.logWithCurrentUser("Equipment", saved.getId(), "SCRAP", "设备报废: " + dto.getReason());
        operationLogService.logWithCurrentUser("ScrapRecord", savedScrap.getId(), "CREATE", "设备报废记录: 原因=" + dto.getReason());
        return saved;
    }

    // 历史记录查询：位置迁移
    public java.util.List<PlacementLog> getPlacementHistory(Long equipmentId) {
        return placementLogRepository.findByEquipmentIdOrderByMovedAtDesc(equipmentId);
    }

    // 历史记录查询：借用记录
    public java.util.List<BorrowRecord> getBorrowHistory(Long equipmentId) {
        return borrowRecordRepository.findByEquipmentIdOrderByBorrowDateDesc(equipmentId);
    }

    // 历史记录查询：维修记录
    public java.util.List<MaintenanceRecord> getMaintenanceHistory(Long equipmentId) {
        return maintenanceRecordRepository.findByEquipmentIdOrderByStartAtDesc(equipmentId);
    }
}