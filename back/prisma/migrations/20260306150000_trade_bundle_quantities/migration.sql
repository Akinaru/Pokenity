SET @schema_name := DATABASE();

SET @has_col := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'trade_requested_items'
    AND COLUMN_NAME = 'is_shiny'
);
SET @sql := IF(
  @has_col = 0,
  'ALTER TABLE `trade_requested_items` ADD COLUMN `is_shiny` BOOLEAN NOT NULL DEFAULT false',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_col := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'trade_requested_items'
    AND COLUMN_NAME = 'quantity'
);
SET @sql := IF(
  @has_col = 0,
  'ALTER TABLE `trade_requested_items` ADD COLUMN `quantity` INT NOT NULL DEFAULT 1',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_table := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.TABLES
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'trade_offered_items'
);
SET @sql := IF(
  @has_table = 0,
  'CREATE TABLE `trade_offered_items` (
    `id` CHAR(36) NOT NULL,
    `trade_id` CHAR(36) NOT NULL,
    `resource_type` ENUM(''POKEMON'', ''ITEM'', ''MACHINE'') NOT NULL DEFAULT ''POKEMON'',
    `resource_id` INT NOT NULL,
    `resource_name` VARCHAR(120) NOT NULL,
    `is_shiny` BOOLEAN NOT NULL DEFAULT false,
    `quantity` INT NOT NULL DEFAULT 1,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    INDEX `trade_offered_items_trade_id_idx`(`trade_id`),
    CONSTRAINT `trade_offered_items_trade_id_fkey`
      FOREIGN KEY (`trade_id`) REFERENCES `trades`(`id`)
      ON DELETE CASCADE ON UPDATE CASCADE
  )',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
