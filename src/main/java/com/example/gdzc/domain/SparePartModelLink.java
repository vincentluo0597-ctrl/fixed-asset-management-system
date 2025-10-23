package com.example.gdzc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spare_part_model_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePartModelLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "part_id", nullable = false)
    private Long partId;

    @Column(name = "equipment_model", length = 100, nullable = false)
    private String equipmentModel; // 适配的设备型号

    @Column(name = "remarks", length = 500)
    private String remarks;
}