package com.example.gdzc.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "equipment_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    @NotBlank(message = "分类编码不能为空")
    private String code; // 分类编码

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "分类名称不能为空")
    private String name; // 分类名称

    @Column(name = "parent_id")
    private Long parentId; // 父分类ID（支持树形结构）

    @Column(name = "level")
    private Integer level; // 分类级别

    @Column(name = "description", length = 500)
    private String description; // 分类描述

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @NotNull(message = "设备类型不能为空")
    private EquipmentType type; // 设备类型（教学设备、科研设备、办公设备等）

    @Column(name = "sort_order")
    private Integer sortOrder; // 排序

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 是否启用

    @Column(name = "remarks", length = 500)
    private String remarks; // 备注

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private List<EquipmentCategory> children;
}