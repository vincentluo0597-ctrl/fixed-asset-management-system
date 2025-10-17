package com.example.gdzc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "equipment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "设备名称不能为空")
    private String name;

    @Column(name = "model", length = 100)
    private String model; // 型号

    @Column(name = "specifications", length = 500)
    private String specifications; // 规格

    @Column(name = "brand", length = 100)
    private String brand; // 品牌

    @Column(name = "category_id")
    private Long categoryId; // 分类ID

    @Column(name = "supplier_id")
    private Long supplierId; // 供应商ID

    @Column(name = "purchase_contract_id")
    private Long purchaseContractId; // 采购合同ID

    @Column(name = "technical_parameters", length = 1000)
    private String technicalParameters; // 技术参数

    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice; // 采购价格

    @Column(name = "purchase_date")
    private LocalDate purchaseDate; // 采购日期

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20)
    private EquipmentSource source; // 设备来源（采购、捐赠、调拨）

    @Column(name = "serial_number", length = 100)
    private String serialNumber; // 序列号

    @Column(name = "asset_number", length = 100, unique = true)
    private String assetNumber; // 资产编号

    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths; // 保修期（月）

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate; // 保修到期日期

    @Column(name = "image_urls", length = 2000)
    private String imageUrls; // 设备图片URL（多个用逗号分隔）

    @Column(name = "document_urls", length = 2000)
    private String documentUrls; // 技术文档URL（多个用逗号分隔）

    @Column(name = "manual_url", length = 500)
    private String manualUrl; // 使用说明书URL

    @Column(name = "keeper_id")
    private Long keeperId; // 当前保管人（用户ID）

    @Column(name = "location_id")
    private Long locationId; // 当前位置（位置ID）

    @Column(name = "current_location_id")
    private Long currentLocationId; // 当前位置（用于设备调用跟踪）

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "设备状态不能为空")
    private EquipmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", length = 20)
    private EquipmentCurrentStatus currentStatus = EquipmentCurrentStatus.AVAILABLE; // 设备当前状态（可用、已借出、维修中、已报废）

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate; // 生产日期

    @Column(name = "service_life_years")
    private Integer serviceLifeYears; // 使用年限

    @Column(name = "depreciation_rate", precision = 5, scale = 4)
    private BigDecimal depreciationRate; // 折旧率

    @Column(name = "current_value", precision = 10, scale = 2)
    private BigDecimal currentValue; // 当前价值

    @Column(name = "remarks", length = 1000)
    private String remarks; // 备注

    // 数量：用于盘库统计与批量入库场景
    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "数量不能为负数")
    @Column(name = "quantity")
    private Integer quantity; // 设备数量（默认 1）
}