CREATE TABLE IF NOT EXISTS scrap_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    reason VARCHAR(1024) NOT NULL,
    scrapped_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_scrap_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id)
);