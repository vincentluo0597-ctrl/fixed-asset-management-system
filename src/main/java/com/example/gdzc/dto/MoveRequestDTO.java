package com.example.gdzc.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveRequestDTO {
    @NotNull
    private Long toLocationId;
}