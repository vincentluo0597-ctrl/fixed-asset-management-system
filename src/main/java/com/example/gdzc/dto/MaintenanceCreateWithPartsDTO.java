package com.example.gdzc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MaintenanceCreateWithPartsDTO {
    @NotNull
    private Long equipmentId;

    @NotBlank
    private String description;

    // 维修中预计或实际使用的备件（行项目）
    private List<UsedSparePartDTO> usedParts;

    // 是否在创建时实时扣减库存，默认 true
    private Boolean deductStock = Boolean.TRUE;
}