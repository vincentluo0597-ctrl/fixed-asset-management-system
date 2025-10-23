package com.example.gdzc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consumable_replacement_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumableReplacementRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "consumable_id", nullable = false)
    private Long consumableId;

    @NotNull
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "replaced_at", nullable = false)
    private LocalDateTime replacedAt;

    @Column(name = "next_due_at")
    private LocalDateTime nextDueAt; // 根据寿命计算出的下次更换时间（若以时间度量）

    @Column(name = "usage_value")
    private Integer usageValue; // 使用值（若以小时/次数等度量）

    @Column(name = "note", length = 1000)
    private String note;

    @PrePersist
    public void prePersist() {
        if (replacedAt == null) replacedAt = LocalDateTime.now();
    }
}