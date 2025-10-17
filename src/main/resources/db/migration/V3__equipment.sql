CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    keeper_id BIGINT,
    location_id BIGINT,
    status VARCHAR(20) NOT NULL
);