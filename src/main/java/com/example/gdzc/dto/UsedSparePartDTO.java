package com.example.gdzc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsedSparePartDTO {
    @NotNull
    private Long partId;

    private Long locationId; // 可选：不填则使用备件默认库位

    @NotNull
    @Min(1)
    private Integer quantity;
}