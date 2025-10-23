package com.example.gdzc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spare_part_inventories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePartInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "part_id", nullable = false)
    private Long partId;

    @NotNull
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Builder.Default
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}