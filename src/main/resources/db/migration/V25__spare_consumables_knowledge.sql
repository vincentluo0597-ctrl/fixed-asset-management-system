-- 创建备件、耗材、知识库与设备文档相关表

-- 备件主数据
CREATE TABLE IF NOT EXISTS spare_parts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    model VARCHAR(100),
    specifications VARCHAR(500),
    unit VARCHAR(20),
    category VARCHAR(100),
    supplier_id BIGINT,
    safety_stock INT DEFAULT 0,
    total_stock INT DEFAULT 0,
    default_location_id BIGINT,
    bin_code VARCHAR(100),
    remarks VARCHAR(1000),
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 备件分库位库存
CREATE TABLE IF NOT EXISTS spare_part_inventories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    part_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    updated_at DATETIME,
    INDEX idx_spi_part(part_id),
    INDEX idx_spi_location(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 备件库存流水
CREATE TABLE IF NOT EXISTS spare_part_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    part_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    location_id BIGINT,
    ref_maintenance_id BIGINT,
    note VARCHAR(1024),
    created_at DATETIME NOT NULL,
    INDEX idx_spt_part(part_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 备件与设备型号关联
CREATE TABLE IF NOT EXISTS spare_part_model_links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    part_id BIGINT NOT NULL,
    equipment_model VARCHAR(100) NOT NULL,
    remarks VARCHAR(500),
    INDEX idx_spml_model(equipment_model),
    INDEX idx_spml_part(part_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 耗材主数据
CREATE TABLE IF NOT EXISTS consumables (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    unit VARCHAR(20),
    life_type VARCHAR(20),
    life_value INT,
    safety_stock INT DEFAULT 0,
    total_stock INT DEFAULT 0,
    default_location_id BIGINT,
    remarks VARCHAR(1000),
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 耗材更换记录
CREATE TABLE IF NOT EXISTS consumable_replacement_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consumable_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    replaced_at DATETIME NOT NULL,
    next_due_at DATETIME,
    usage_value INT,
    note VARCHAR(1000),
    INDEX idx_crr_consumable(consumable_id),
    INDEX idx_crr_equipment(equipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 故障知识库案例
CREATE TABLE IF NOT EXISTS fault_cases (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_id BIGINT,
    title VARCHAR(200),
    phenomenon TEXT,
    cause TEXT,
    solution TEXT,
    used_spare_parts TEXT,
    tags VARCHAR(500),
    attachments TEXT,
    created_by VARCHAR(100),
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 设备档案文档
CREATE TABLE IF NOT EXISTS equipment_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_id BIGINT,
    title VARCHAR(200),
    doc_type VARCHAR(20),
    file_url VARCHAR(1000),
    uploaded_at DATETIME,
    uploaded_by VARCHAR(100),
    remarks VARCHAR(1000),
    INDEX idx_ed_equipment(equipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;