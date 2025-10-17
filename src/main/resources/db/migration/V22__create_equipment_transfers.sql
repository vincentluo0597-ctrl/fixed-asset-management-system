-- 设备调用记录表
CREATE TABLE equipment_transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    transfer_type VARCHAR(50) NOT NULL, -- TRANSFER(调拨), LOAN(借出), RETURN(归还), MOVE(移动)
    from_location_id BIGINT,
    to_location_id BIGINT,
    from_user_id BIGINT,
    to_user_id BIGINT,
    transfer_reason TEXT NOT NULL, -- 调用说明（强制填写）
    transfer_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_return_date DATETIME, -- 预期归还时间（借出时）
    actual_return_date DATETIME, -- 实际归还时间
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, COMPLETED, CANCELLED
    created_by VARCHAR(100) NOT NULL, -- 调用人（自动填写）
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    FOREIGN KEY (from_location_id) REFERENCES locations(id),
    FOREIGN KEY (to_location_id) REFERENCES locations(id),
    FOREIGN KEY (from_user_id) REFERENCES users(id),
    FOREIGN KEY (to_user_id) REFERENCES users(id),
    INDEX idx_equipment_id (equipment_id),
    INDEX idx_transfer_date (transfer_date),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 添加设备状态字段，用于跟踪设备当前状态
ALTER TABLE equipment ADD COLUMN current_status VARCHAR(20) DEFAULT 'AVAILABLE' 
    COMMENT 'AVAILABLE(可用), LOANED(已借出), MAINTENANCE(维修中), RETIRED(已报废)';

-- 添加设备当前位置字段
ALTER TABLE equipment ADD COLUMN current_location_id BIGINT;
ALTER TABLE equipment ADD FOREIGN KEY (current_location_id) REFERENCES locations(id);
ALTER TABLE equipment ADD INDEX idx_current_location (current_location_id);
ALTER TABLE equipment ADD INDEX idx_current_status (current_status);