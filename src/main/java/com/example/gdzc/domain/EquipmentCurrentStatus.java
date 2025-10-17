package com.example.gdzc.domain;

public enum EquipmentCurrentStatus {
    AVAILABLE("可用"),
    LOANED("已借出"),
    MAINTENANCE("维修中"),
    RETIRED("已报废");
    
    private final String description;
    
    EquipmentCurrentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}