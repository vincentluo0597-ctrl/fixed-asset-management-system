-- Create equipment_categories table to support EquipmentCategory entity and hierarchy

CREATE TABLE IF NOT EXISTS equipment_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    parent_id BIGINT NULL,
    level INT NULL,
    description VARCHAR(500),
    type VARCHAR(100),
    sort_order INT DEFAULT 0,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    remarks VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
);

-- Unique code and basic index for hierarchy lookups
CREATE UNIQUE INDEX ux_equipment_categories_code ON equipment_categories(code);
CREATE INDEX idx_equipment_categories_parent_id ON equipment_categories(parent_id);