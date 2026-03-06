-- Add columns in an idempotent way (works even after partial failed migration)
SET @schema_name := DATABASE();

SET @has_col := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'inventory_items'
    AND COLUMN_NAME = 'is_shiny'
);
SET @sql := IF(
  @has_col = 0,
  'ALTER TABLE `inventory_items` ADD COLUMN `is_shiny` BOOLEAN NOT NULL DEFAULT false',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_col := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'box_openings'
    AND COLUMN_NAME = 'is_shiny'
);
SET @sql := IF(
  @has_col = 0,
  'ALTER TABLE `box_openings` ADD COLUMN `is_shiny` BOOLEAN NOT NULL DEFAULT false',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_col := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'trades'
    AND COLUMN_NAME = 'offered_is_shiny'
);
SET @sql := IF(
  @has_col = 0,
  'ALTER TABLE `trades` ADD COLUMN `offered_is_shiny` BOOLEAN NOT NULL DEFAULT false',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_col := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'trades'
    AND COLUMN_NAME = 'received_is_shiny'
);
SET @sql := IF(
  @has_col = 0,
  'ALTER TABLE `trades` ADD COLUMN `received_is_shiny` BOOLEAN NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Redefine unique index for inventory variants (normal/shiny)
SET @has_old_idx := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'inventory_items'
    AND INDEX_NAME = 'inventory_items_user_id_resource_type_resource_id_key'
);
SET @sql := IF(
  @has_old_idx > 0,
  'DROP INDEX `inventory_items_user_id_resource_type_resource_id_key` ON `inventory_items`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_new_idx := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'inventory_items'
    AND INDEX_NAME = 'inventory_items_user_id_resource_type_resource_id_is_shiny_key'
);
SET @sql := IF(
  @has_new_idx = 0,
  'CREATE UNIQUE INDEX `inventory_items_user_id_resource_type_resource_id_is_shiny_key` ON `inventory_items`(`user_id`, `resource_type`, `resource_id`, `is_shiny`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
