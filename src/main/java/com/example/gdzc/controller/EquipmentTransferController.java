package com.example.gdzc.controller;

import com.example.gdzc.domain.EquipmentTransfer;
import com.example.gdzc.dto.EquipmentTransferDTO;
import com.example.gdzc.service.EquipmentTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/equipment-transfers")
@RequiredArgsConstructor
public class EquipmentTransferController {
    
    private final EquipmentTransferService transferService;
    
    /**
     * 获取所有设备调用记录
     */
    @GetMapping
    public ResponseEntity<List<EquipmentTransfer>> getAllTransfers() {
        return ResponseEntity.ok(transferService.getAllTransfers());
    }
    
    /**
     * 分页获取设备调用记录
     */
    @GetMapping("/page")
    public ResponseEntity<Page<EquipmentTransfer>> getTransferPage(
            Pageable pageable,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status) {
        EquipmentTransfer.TransferType transferType = null;
        EquipmentTransfer.TransferStatus transferStatus = null;

        if (type != null && !type.isBlank()) {
            try {
                transferType = EquipmentTransfer.TransferType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
        }

        if (status != null && !status.isBlank()) {
            try {
                transferStatus = EquipmentTransfer.TransferStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok(transferService.getTransferPage(pageable, transferType, transferStatus));
    }
    
    /**
     * 根据设备ID获取调用记录
     */
    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<EquipmentTransfer>> getTransfersByEquipmentId(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(transferService.getTransfersByEquipmentId(equipmentId));
    }
    
    /**
     * 根据当前用户获取调用记录（调用人自动填写功能）
     */
    @GetMapping("/my-transfers")
    public ResponseEntity<Page<EquipmentTransfer>> getMyTransfers(Pageable pageable) {
        String currentUsername = getCurrentUsername();
        return ResponseEntity.ok(transferService.getTransfersByCreator(currentUsername, pageable));
    }
    
    /**
     * 创建设备调用记录
     * 调用人自动填写：从当前登录用户获取
     */
    @PostMapping
    public ResponseEntity<EquipmentTransfer> createTransfer(@Valid @RequestBody EquipmentTransferDTO dto) {
        String currentUsername = getCurrentUsername();
        
        // 验证调用说明不能为空（强制填写验证）
        if (dto.getTransferReason() == null || dto.getTransferReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        
        EquipmentTransfer transfer = transferService.createTransfer(dto, currentUsername);
        return ResponseEntity.ok(transfer);
    }
    
    /**
     * 完成设备调用（用于归还操作）
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<EquipmentTransfer> completeTransfer(@PathVariable Long id) {
        String currentUsername = getCurrentUsername();
        EquipmentTransfer completedTransfer = transferService.completeTransfer(id, currentUsername);
        return ResponseEntity.ok(completedTransfer);
    }
    
    /**
     * 取消设备调用
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTransfer(@PathVariable Long id) {
        String currentUsername = getCurrentUsername();
        transferService.cancelTransfer(id, currentUsername);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取逾期借出的设备
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<EquipmentTransfer>> getOverdueLoans() {
        return ResponseEntity.ok(transferService.getOverdueLoans());
    }
    
    /**
     * 获取设备调用统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<EquipmentTransferService.EquipmentTransferStatistics> getTransferStatistics() {
        return ResponseEntity.ok(transferService.getTransferStatistics());
    }
    
    /**
     * 获取当前登录用户名
     * 实现调用人自动填写功能
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system"; // 默认系统用户
    }
    
    /**
     * 获取设备调用类型枚举
     */
    @GetMapping("/types")
    public ResponseEntity<EquipmentTransfer.TransferType[]> getTransferTypes() {
        return ResponseEntity.ok(EquipmentTransfer.TransferType.values());
    }
    
    /**
     * 获取设备调用状态枚举
     */
    @GetMapping("/statuses")
    public ResponseEntity<EquipmentTransfer.TransferStatus[]> getTransferStatuses() {
        return ResponseEntity.ok(EquipmentTransfer.TransferStatus.values());
    }
}