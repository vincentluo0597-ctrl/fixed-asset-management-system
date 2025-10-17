package com.example.gdzc.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BorrowRequestDTO {
    @NotNull
    private Long borrowerId;
    private LocalDate expectedReturnDate;
}