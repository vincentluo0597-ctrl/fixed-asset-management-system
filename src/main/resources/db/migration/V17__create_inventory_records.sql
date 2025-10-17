-- Create inventory_records table to support InventoryRecord entity

CREATE TABLE IF NOT EXISTS inventory_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    source VARCHAR(100),
    note VARCHAR(1024),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);