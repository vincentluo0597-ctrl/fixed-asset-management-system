package com.example.gdzc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaintenanceCompleteDTO {
    @NotBlank
    private String result;
}