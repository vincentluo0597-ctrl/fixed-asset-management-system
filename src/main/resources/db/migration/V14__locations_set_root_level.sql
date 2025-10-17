-- Set level=0 for root locations where level is currently NULL
UPDATE locations SET level = 0 WHERE parent_id IS NULL AND level IS NULL;