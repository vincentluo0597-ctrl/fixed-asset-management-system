package com.example.gdzc.dto;

import lombok.Data;

@Data
public class InventoryRecordDTO {
    private String source;
    private String note;
    // 可选：是否调整数量（为兼容前端简单传参，这里同时支持仅传 newQuantity 来判断调整）
    private Boolean adjustQuantity;
    // 可选：调整后的数量（非负整数）
    private Integer newQuantity;
    // 可选：调整原因（当调整数量时建议必填）
    private String adjustReason;
}