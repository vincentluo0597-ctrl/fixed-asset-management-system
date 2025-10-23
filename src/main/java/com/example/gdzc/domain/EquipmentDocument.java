package com.example.gdzc.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "title", length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", length = 20)
    private DocType docType; // MANUAL, DRAWING, SPEC, OTHER

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @PrePersist
    public void prePersist() { if (uploadedAt == null) uploadedAt = LocalDateTime.now(); }

    public enum DocType { MANUAL, DRAWING, SPEC, OTHER }
}