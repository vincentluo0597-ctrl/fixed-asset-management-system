package com.example.gdzc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spare_part_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePartTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "part_id", nullable = false)
    private Long partId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type; // IN, OUT, ADJUST

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 正数；出库由服务层转为负向更新

    @Column(name = "location_id")
    private Long locationId; // 发生变动的库位

    @Column(name = "ref_maintenance_id")
    private Long refMaintenanceId; // 关联的维修记录（出库时）

    @Column(name = "note", length = 1024)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public enum TransactionType {
        IN, OUT, ADJUST
    }
}