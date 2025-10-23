package com.example.gdzc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consumables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consumable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    @NotBlank(message = "耗材编码不能为空")
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    @NotBlank(message = "耗材名称不能为空")
    private String name;

    @Column(name = "unit", length = 20)
    private String unit; // 计量单位（瓶、升、个）

    @Enumerated(EnumType.STRING)
    @Column(name = "life_type", length = 20)
    private LifeType lifeType; // 使用寿命的度量类型

    @Column(name = "life_value")
    private Integer lifeValue; // 使用寿命的数值

    @Builder.Default
    @Column(name = "safety_stock")
    private Integer safetyStock = 0;

    @Builder.Default
    @Column(name = "total_stock")
    private Integer totalStock = 0;

    @Column(name = "default_location_id")
    private Long defaultLocationId;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum LifeType {
        DAYS, HOURS, CYCLES
    }
}