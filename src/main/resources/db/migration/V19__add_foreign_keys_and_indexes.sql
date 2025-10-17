-- Add foreign keys and indexes after tables are present

-- Equipment indexes
CREATE UNIQUE INDEX ux_equipment_asset_number ON equipment(asset_number);
CREATE INDEX idx_equipment_supplier_id ON equipment(supplier_id);
CREATE INDEX idx_equipment_category_id ON equipment(category_id);
CREATE INDEX idx_equipment_location_id ON equipment(location_id);

-- Add foreign keys (ensure existing data compatible)
ALTER TABLE equipment
    ADD CONSTRAINT fk_equipment_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    ADD CONSTRAINT fk_equipment_category FOREIGN KEY (category_id) REFERENCES equipment_categories(id),
    ADD CONSTRAINT fk_equipment_location FOREIGN KEY (location_id) REFERENCES locations(id);

-- Equipment categories self reference (optional, enables cascaded hierarchy integrity)
ALTER TABLE equipment_categories
    ADD CONSTRAINT fk_equipment_categories_parent FOREIGN KEY (parent_id) REFERENCES equipment_categories(id);

-- Inventory foreign key
ALTER TABLE inventory_records
    ADD CONSTRAINT fk_inventory_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id);