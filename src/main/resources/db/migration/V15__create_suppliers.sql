-- Create suppliers table to support Supplier entity and service logic
-- MySQL 8, default schema gdzc

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(200),
    address VARCHAR(500),
    website VARCHAR(200),
    type VARCHAR(50),
    credit_rating VARCHAR(50),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    remarks VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
);

-- Unique supplier code for business uniqueness (Flyway ensures single execution)
CREATE UNIQUE INDEX ux_suppliers_code ON suppliers(code);