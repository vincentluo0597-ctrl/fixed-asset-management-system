package com.example.gdzc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScrapRequestDTO {
    @NotBlank
    private String reason;
}