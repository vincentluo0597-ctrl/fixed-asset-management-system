package com.example.gdzc.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fault_cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaultCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "title", length = 200)
    private String title; // 案例标题

    @Column(name = "phenomenon", length = 2000)
    private String phenomenon; // 故障现象

    @Column(name = "cause", length = 2000)
    private String cause; // 原因分析

    @Column(name = "solution", length = 4000)
    private String solution; // 解决方案

    @Column(name = "used_spare_parts", length = 4000)
    private String usedSpareParts; // JSON：[{partId, quantity}, ...]

    @Column(name = "tags", length = 500)
    private String tags; // 逗号分隔标签

    @Column(name = "attachments", length = 4000)
    private String attachments; // 文档/图片URL，逗号分隔

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}