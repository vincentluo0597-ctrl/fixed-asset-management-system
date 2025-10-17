package com.example.gdzc.dto;

import com.example.gdzc.domain.LocationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationTreeDTO {
    private Long id;
    private String name;
    private LocationType type;
    private Long managerId;
    private Long parentId;
    private Integer level;
    private List<LocationTreeDTO> children;
}