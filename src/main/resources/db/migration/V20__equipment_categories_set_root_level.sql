-- Set level=0 for root categories with NULL level to avoid nulls in interfaces

UPDATE equipment_categories
SET level = 0
WHERE parent_id IS NULL AND level IS NULL;