-- 修复：确保设备调用记录表存在，并补齐 equipment 表的状态与位置字段、外键与索引

-- 1) 如果不存在则创建设备调用记录表
CREATE TABLE IF NOT EXISTS equipment_transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    transfer_type VARCHAR(50) NOT NULL, -- TRANSFER(调拨), LOAN(借出), RETURN(G归还), MOVE(移动)
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

-- 2) 补齐 equipment 表上的新增字段与约束（均为幂等检查）
SET @dbname = DATABASE();
SET @tablename = 'equipment';

-- 2.1 当前状态字段
SET @columnname = 'current_status';
SET @preparedStatement = (
  SELECT IF(
    (
      SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA COLLATE utf8_general_ci = DATABASE() COLLATE utf8_general_ci
        AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname,
           " VARCHAR(20) DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE(可用), LOANED(已借出), MAINTENANCE(维修中), RETIRED(已报废)'")
  )
);
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2.2 当前位置字段
SET @columnname = 'current_location_id';
SET @preparedStatement = (
  SELECT IF(
    (
      SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA COLLATE utf8_general_ci = DATABASE() COLLATE utf8_general_ci
        AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT')
  )
);
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2.3 外键（equipment.current_location_id -> locations.id）
-- 若已存在，跳过；若不存在则添加一个具名外键，避免重复
SET @fkExists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA COLLATE utf8_general_ci = DATABASE() COLLATE utf8_general_ci
    AND TABLE_NAME = 'equipment'
    AND COLUMN_NAME = 'current_location_id'
    AND REFERENCED_TABLE_SCHEMA COLLATE utf8_general_ci = DATABASE() COLLATE utf8_general_ci
    AND REFERENCED_TABLE_NAME = 'locations'
    AND REFERENCED_COLUMN_NAME = 'id'
);
SET @preparedStatement = (
  SELECT IF(
    @fkExists > 0,
    'SELECT 1',
    'ALTER TABLE equipment ADD CONSTRAINT fk_equipment_current_location FOREIGN KEY (current_location_id) REFERENCES locations(id)'
  )
);
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2.4 索引：idx_current_location
SET @indexExists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA COLLATE utf8_general_ci = DATABASE() COLLATE utf8_general_ci
    AND TABLE_NAME = 'equipment' AND INDEX_NAME = 'idx_current_location'
);
SET @preparedStatement = (
  SELECT IF(
    @indexExists > 0,
    'SELECT 1',
    'ALTER TABLE equipment ADD INDEX idx_current_location (current_location_id)'
  )
);
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2.5 索引：idx_current_status
SET @indexExists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA COLLATE utf8_general_ci = DATABASE() COLLATE utf8_general_ci
    AND TABLE_NAME = 'equipment' AND INDEX_NAME = 'idx_current_status'
);
SET @preparedStatement = (
  SELECT IF(
    @indexExists > 0,
    'SELECT 1',
    'ALTER TABLE equipment ADD INDEX idx_current_status (current_status)'
  )
);
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 说明：以上所有变更均采用“存在则跳过，不存在则创建”的幂等策略，
-- 可在已有数据的情况下安全补齐缺失结构，避免 Flyway 重复执行报错。