CREATE TABLE IF NOT EXISTS placement_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    from_location_id BIGINT,
    to_location_id BIGINT NOT NULL,
    moved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_placement_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT fk_placement_from_location FOREIGN KEY (from_location_id) REFERENCES locations(id),
    CONSTRAINT fk_placement_to_location FOREIGN KEY (to_location_id) REFERENCES locations(id)
);