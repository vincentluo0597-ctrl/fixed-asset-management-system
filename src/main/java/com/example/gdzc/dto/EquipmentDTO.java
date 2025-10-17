package com.example.gdzc.dto;

import com.example.gdzc.domain.EquipmentSource;
import com.example.gdzc.domain.EquipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EquipmentDTO {
    @NotBlank(message = "设备名称不能为空")
    private String name;
    
    private String model; // 型号
    
    private String specifications; // 规格
    
    private String brand; // 品牌
    
    private Long categoryId; // 分类ID
    
    private Long supplierId; // 供应商ID
    
    private Long purchaseContractId; // 采购合同ID
    
    private String technicalParameters; // 技术参数
    
    private BigDecimal purchasePrice; // 采购价格
    
    private LocalDate purchaseDate; // 采购日期
    
    private EquipmentSource source; // 设备来源
    
    private String serialNumber; // 序列号
    
    private String assetNumber; // 资产编号
    
    private Integer warrantyPeriodMonths; // 保修期（月）
    
    private LocalDate warrantyExpiryDate; // 保修到期日期
    
    private String imageUrls; // 设备图片URL
    
    private String documentUrls; // 技术文档URL
    
    private String manualUrl; // 使用说明书URL
    
    private Long keeperId; // 当前保管人
    
    private Long locationId; // 当前位置
    
    @NotNull(message = "设备状态不能为空")
    private EquipmentStatus status;
    
    private LocalDate manufactureDate; // 生产日期
    
    private Integer serviceLifeYears; // 使用年限
    
    private BigDecimal depreciationRate; // 折旧率
    
    private BigDecimal currentValue; // 当前价值
    
    private String remarks; // 备注

    @Min(value = 0, message = "数量不能为负数")
    private Integer quantity; // 数量（默认 1，不传时后端按 1 处理）
}