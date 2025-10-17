CREATE TABLE IF NOT EXISTS borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    borrower_id BIGINT NOT NULL,
    borrow_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_return_date DATE,
    return_date TIMESTAMP,
    CONSTRAINT fk_borrow_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id)
);