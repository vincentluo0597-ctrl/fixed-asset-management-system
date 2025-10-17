package com.example.gdzc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "placement_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "from_location_id")
    private Long fromLocationId;

    @NotNull
    @Column(name = "to_location_id", nullable = false)
    private Long toLocationId;

    @Column(name = "moved_at", nullable = false)
    private LocalDateTime movedAt;

    @PrePersist
    public void prePersist() {
        if (movedAt == null) {
            movedAt = LocalDateTime.now();
        }
    }
}