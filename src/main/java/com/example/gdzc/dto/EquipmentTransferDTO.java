package com.example.gdzc.dto;

import com.example.gdzc.domain.EquipmentTransfer;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class EquipmentTransferDTO {
    
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;
    
    @NotNull(message = "调用类型不能为空")
    private EquipmentTransfer.TransferType transferType;
    
    private Long fromLocationId; // 起始位置ID
    
    private Long toLocationId; // 目标位置ID
    
    private Long fromUserId; // 起始用户ID
    
    private Long toUserId; // 目标用户ID
    
    @NotBlank(message = "调用说明不能为空")
    private String transferReason; // 调用说明（强制填写）
    
    private LocalDateTime expectedReturnDate; // 预期归还日期（借出时）
    
    private LocalDateTime transferDate; // 调用日期
    
    // 构造方法
    public EquipmentTransferDTO() {}
    
    public EquipmentTransferDTO(Long equipmentId, EquipmentTransfer.TransferType transferType, String transferReason) {
        this.equipmentId = equipmentId;
        this.transferType = transferType;
        this.transferReason = transferReason;
    }
}