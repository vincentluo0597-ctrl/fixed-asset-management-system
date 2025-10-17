-- Ensure remaining tables use utf8mb4 Unicode collation
SET @db := DATABASE();

-- Apply conversion only if table exists to avoid migration failures
SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=@db AND table_name='operation_logs'),
    'ALTER TABLE operation_logs CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=@db AND table_name='locations'),
    'ALTER TABLE locations CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=@db AND table_name='equipment'),
    'ALTER TABLE equipment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=@db AND table_name='inventory_records'),
    'ALTER TABLE inventory_records CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=@db AND table_name='borrow_records'),
    'ALTER TABLE borrow_records CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=@db AND table_name='placement_logs'),
    'ALTER TABLE placement_logs CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=@db AND table_name='maintenance_records'),
    'ALTER TABLE maintenance_records CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=@db AND table_name='scrap_records'),
    'ALTER TABLE scrap_records CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;