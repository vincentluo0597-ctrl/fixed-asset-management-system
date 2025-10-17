package com.example.gdzc.service;

import com.example.gdzc.domain.*;
import com.example.gdzc.dto.EquipmentTransferDTO;
import com.example.gdzc.repository.EquipmentRepository;
import com.example.gdzc.repository.EquipmentTransferRepository;
import com.example.gdzc.repository.LocationRepository;
import com.example.gdzc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentTransferService {
    
    private final EquipmentTransferRepository transferRepository;
    private final EquipmentRepository equipmentRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final OperationLogService operationLogService;
    
    /**
     * 获取所有设备调用记录
     */
    public List<EquipmentTransfer> getAllTransfers() {
        return transferRepository.findAllWithAssociations();
    }
    
    /**
     * 分页获取设备调用记录（支持按类型或状态筛选）
     */
    public Page<EquipmentTransfer> getTransferPage(Pageable pageable) {
        return transferRepository.findAllWithAssociations(pageable);
    }

    /**
     * 分页获取设备调用记录（支持按类型或状态筛选）
     */
    public Page<EquipmentTransfer> getTransferPage(Pageable pageable,
                                                  EquipmentTransfer.TransferType transferType,
                                                  EquipmentTransfer.TransferStatus status) {
        if (transferType != null) {
            return transferRepository.findByTransferTypeOrderByTransferDateDescWithAssociations(transferType, pageable);
        }
        if (status != null) {
            return transferRepository.findByStatusOrderByTransferDateDescWithAssociations(status, pageable);
        }
        return transferRepository.findAllWithAssociations(pageable);
    }
    
    /**
     * 根据设备ID获取调用记录
     */
    public List<EquipmentTransfer> getTransfersByEquipmentId(Long equipmentId) {
        return transferRepository.findByEquipmentIdOrderByTransferDateDescWithAssociations(equipmentId);
    }
    
    /**
     * 根据创建者获取调用记录
     */
    public Page<EquipmentTransfer> getTransfersByCreator(String createdBy, Pageable pageable) {
        return transferRepository.findByCreatedByOrderByTransferDateDescWithAssociations(createdBy, pageable);
    }
    
    /**
     * 创建设备调用记录
     */
    @Transactional
    public EquipmentTransfer createTransfer(EquipmentTransferDTO dto, String currentUsername) {
        // 验证设备存在
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        
        // 验证调用说明不能为空
        if (dto.getTransferReason() == null || dto.getTransferReason().trim().isEmpty()) {
            throw new IllegalArgumentException("调用说明不能为空");
        }
        
        // 创建设备调用记录
        EquipmentTransfer transfer = new EquipmentTransfer();
        transfer.setEquipment(equipment);
        transfer.setTransferType(dto.getTransferType());
        transfer.setTransferReason(dto.getTransferReason());
        transfer.setCreatedBy(currentUsername); // 自动填写调用人
        
        // 设置位置信息
        if (dto.getFromLocationId() != null) {
            Location fromLocation = locationRepository.findById(dto.getFromLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("起始位置不存在"));
            transfer.setFromLocation(fromLocation);
        }
        
        if (dto.getToLocationId() != null) {
            Location toLocation = locationRepository.findById(dto.getToLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("目标位置不存在"));
            transfer.setToLocation(toLocation);
        }
        
        // 设置用户信息
        if (dto.getFromUserId() != null) {
            User fromUser = userRepository.findById(dto.getFromUserId())
                    .orElseThrow(() -> new IllegalArgumentException("起始用户不存在"));
            transfer.setFromUser(fromUser);
        }
        
        if (dto.getToUserId() != null) {
            User toUser = userRepository.findById(dto.getToUserId())
                    .orElseThrow(() -> new IllegalArgumentException("目标用户不存在"));
            transfer.setToUser(toUser);
        }
        
        // 设置预期归还日期（借出时）
        if (dto.getTransferType() == EquipmentTransfer.TransferType.LOAN) {
            transfer.setExpectedReturnDate(dto.getExpectedReturnDate());
        }
        
        // 保存调用记录
        EquipmentTransfer savedTransfer = transferRepository.save(transfer);
        
        // 更新设备状态和位置
        updateEquipmentStatusAndLocation(equipment, transfer);
        
        // 记录操作日志
        operationLogService.log(currentUsername, "EquipmentTransfer", savedTransfer.getId(), "CREATE", 
                String.format("创建设备调用记录：设备 %s，类型 %s，原因 %s", 
                equipment.getName(), dto.getTransferType().getDescription(), dto.getTransferReason()));
        
        // 保存后重新按ID加载关联对象，返回完整实体，避免序列化懒加载异常
        return transferRepository.findByIdWithAssociations(savedTransfer.getId()).orElse(savedTransfer);
    }
    
    /**
     * 更新设备状态和位置
     */
    private void updateEquipmentStatusAndLocation(Equipment equipment, EquipmentTransfer transfer) {
        EquipmentTransfer.TransferType transferType = transfer.getTransferType();
        
        switch (transferType) {
            case LOAN:
                // 借出：设备状态变为已借出
                equipment.setCurrentStatus(EquipmentCurrentStatus.LOANED);
                if (transfer.getToLocation() != null) {
                    equipment.setCurrentLocationId(transfer.getToLocation().getId());
                }
                break;
                
            case RETURN:
                // 归还：设备状态变为可用
                equipment.setCurrentStatus(EquipmentCurrentStatus.AVAILABLE);
                if (transfer.getToLocation() != null) {
                    equipment.setCurrentLocationId(transfer.getToLocation().getId());
                }
                // 更新实际归还日期
                transfer.setActualReturnDate(LocalDateTime.now());
                transfer.setStatus(EquipmentTransfer.TransferStatus.COMPLETED);
                break;
                
            case TRANSFER:
            case MOVE:
                // 调拨或移动：更新位置
                if (transfer.getToLocation() != null) {
                    equipment.setCurrentLocationId(transfer.getToLocation().getId());
                }
                transfer.setStatus(EquipmentTransfer.TransferStatus.COMPLETED);
                break;
        }
        
        equipmentRepository.save(equipment);
    }
    
    /**
     * 完成设备调用（用于归还操作）
     */
    @Transactional
    public EquipmentTransfer completeTransfer(Long transferId, String currentUsername) {
        EquipmentTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("调用记录不存在"));
        
        if (transfer.getStatus() != EquipmentTransfer.TransferStatus.ACTIVE) {
            throw new IllegalArgumentException("该调用记录已完成或已取消");
        }
        
        // 更新状态为已完成
        transfer.setStatus(EquipmentTransfer.TransferStatus.COMPLETED);
        
        // 更新设备状态
        Equipment equipment = transfer.getEquipment();
        equipment.setCurrentStatus(EquipmentCurrentStatus.AVAILABLE);
        
        equipmentRepository.save(equipment);
        EquipmentTransfer completedTransfer = transferRepository.save(transfer);
        
        operationLogService.log(currentUsername, "EquipmentTransfer", completedTransfer.getId(), "COMPLETE", 
                String.format("完成设备调用记录：设备 %s，调用类型 %s", 
                equipment.getName(), transfer.getTransferType().getDescription()));
        
        // 保存后重新按ID加载关联对象，返回完整实体，避免序列化懒加载异常
        return transferRepository.findByIdWithAssociations(completedTransfer.getId()).orElse(completedTransfer);
    }
    
    /**
     * 取消设备调用
     */
    @Transactional
    public void cancelTransfer(Long transferId, String currentUsername) {
        EquipmentTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("调用记录不存在"));
        
        if (transfer.getStatus() != EquipmentTransfer.TransferStatus.ACTIVE) {
            throw new IllegalArgumentException("该调用记录已完成或已取消");
        }
        
        transfer.setStatus(EquipmentTransfer.TransferStatus.CANCELLED);
        transferRepository.save(transfer);
        
        Equipment equipment = transfer.getEquipment();
        operationLogService.log(currentUsername, "EquipmentTransfer", transfer.getId(), "CANCEL", 
                String.format("取消设备调用记录：设备 %s，调用类型 %s", 
                equipment.getName(), transfer.getTransferType().getDescription()));
    }
    
    /**
     * 获取逾期借出的设备
     */
    public List<EquipmentTransfer> getOverdueLoans() {
        return transferRepository.findOverdueLoans(LocalDateTime.now());
    }
    
    /**
     * 获取设备调用统计信息
     */
    public EquipmentTransferStatistics getTransferStatistics() {
        long totalTransfers = transferRepository.count();
        long activeTransfers = transferRepository.countByStatus(EquipmentTransfer.TransferStatus.ACTIVE);
        long overdueLoans = getOverdueLoans().size();
        
        return new EquipmentTransferStatistics(totalTransfers, activeTransfers, overdueLoans);
    }
    
    /**
     * 统计信息DTO
     */
    public static class EquipmentTransferStatistics {
        private final long totalTransfers;
        private final long activeTransfers;
        private final long overdueLoans;
        
        public EquipmentTransferStatistics(long totalTransfers, long activeTransfers, long overdueLoans) {
            this.totalTransfers = totalTransfers;
            this.activeTransfers = activeTransfers;
            this.overdueLoans = overdueLoans;
        }
        
        public long getTotalTransfers() {
            return totalTransfers;
        }
        
        public long getActiveTransfers() {
            return activeTransfers;
        }
        
        public long getOverdueLoans() {
            return overdueLoans;
        }
    }
}