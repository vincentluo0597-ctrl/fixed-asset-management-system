package com.example.gdzc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "source")
    private String source;

    @Column(name = "note", length = 1024)
    private String note;

    // 若本次盘库进行了数量调整，则记录调整后的数量与原因
    @Column(name = "adjust_after_quantity")
    private Integer adjustAfterQuantity;

    @Column(name = "adjust_reason", length = 1024)
    private String adjustReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}