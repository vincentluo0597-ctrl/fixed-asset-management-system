package com.example.gdzc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scrap_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "reason", length = 1024, nullable = false)
    private String reason;

    @Column(name = "scrapped_at", nullable = false)
    private LocalDateTime scrappedAt;

    @PrePersist
    public void prePersist() {
        if (scrappedAt == null) {
            scrappedAt = LocalDateTime.now();
        }
    }
}