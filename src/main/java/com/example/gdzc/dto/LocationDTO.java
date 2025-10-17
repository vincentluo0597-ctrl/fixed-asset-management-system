package com.example.gdzc.dto;

import com.example.gdzc.domain.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationDTO {
    @NotBlank(message = "位置名称不能为空")
    private String name;

    @NotNull(message = "位置类型不能为空")
    private LocationType type;

    private Long managerId;

    private Long parentId;

    private Integer level;
}