package com.example.gdzc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spare_parts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    @NotBlank(message = "备件编码不能为空")
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    @NotBlank(message = "备件名称不能为空")
    private String name;

    @Column(name = "model", length = 100)
    private String model; // 备件型号

    @Column(name = "specifications", length = 500)
    private String specifications; // 规格参数

    @Column(name = "unit", length = 20)
    private String unit; // 计量单位（件、盒、瓶等）

    @Column(name = "category", length = 100)
    private String category; // 备件类别（可与设备分类不同）

    @Column(name = "supplier_id")
    private Long supplierId; // 默认供应商

    @Builder.Default
    @Column(name = "safety_stock")
    private Integer safetyStock = 0; // 安全库存下限

    @Builder.Default
    @Column(name = "total_stock")
    private Integer totalStock = 0; // 当前总库存（所有库位合计）

    @Column(name = "default_location_id")
    private Long defaultLocationId; // 默认存放位置

    @Column(name = "bin_code", length = 100)
    private String binCode; // 货架/库位编码

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}