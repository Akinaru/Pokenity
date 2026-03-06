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
-- 1) Drop FK on user_id (whatever its constraint name is), because some MySQL
-- versions keep depending on the old unique index.
SET @fk_name := (
  SELECT kcu.CONSTRAINT_NAME
  FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu
  WHERE kcu.TABLE_SCHEMA = @schema_name
    AND kcu.TABLE_NAME = 'inventory_items'
    AND kcu.COLUMN_NAME = 'user_id'
    AND kcu.REFERENCED_TABLE_NAME = 'users'
    AND kcu.REFERENCED_COLUMN_NAME = 'id'
  LIMIT 1
);
SET @sql := IF(
  @fk_name IS NOT NULL,
  CONCAT('ALTER TABLE `inventory_items` DROP FOREIGN KEY `', @fk_name, '`'),
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) Ensure a dedicated index exists for user_id before dropping the old unique.
SET @has_user_idx := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'inventory_items'
    AND INDEX_NAME = 'inventory_items_user_id_idx'
);
SET @sql := IF(
  @has_user_idx = 0,
  'CREATE INDEX `inventory_items_user_id_idx` ON `inventory_items`(`user_id`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) Replace old unique index with new one including is_shiny.
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

-- 4) Recreate FK with a stable name if missing.
SET @has_fk := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu
  WHERE kcu.TABLE_SCHEMA = @schema_name
    AND kcu.TABLE_NAME = 'inventory_items'
    AND kcu.COLUMN_NAME = 'user_id'
    AND kcu.REFERENCED_TABLE_NAME = 'users'
    AND kcu.REFERENCED_COLUMN_NAME = 'id'
);
SET @sql := IF(
  @has_fk = 0,
  'ALTER TABLE `inventory_items` ADD CONSTRAINT `inventory_items_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
