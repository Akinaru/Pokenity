-- AlterTable
ALTER TABLE `inventory_items`
ADD COLUMN IF NOT EXISTS `is_shiny` BOOLEAN NOT NULL DEFAULT false;

-- AlterTable
ALTER TABLE `box_openings`
ADD COLUMN IF NOT EXISTS `is_shiny` BOOLEAN NOT NULL DEFAULT false;

-- AlterTable
ALTER TABLE `trades`
ADD COLUMN IF NOT EXISTS `offered_is_shiny` BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN IF NOT EXISTS `received_is_shiny` BOOLEAN NULL;

-- Redefine unique index for inventory variants (normal/shiny)
DROP INDEX IF EXISTS `inventory_items_user_id_resource_type_resource_id_key` ON `inventory_items`;
DROP INDEX IF EXISTS `inventory_items_user_id_resource_type_resource_id_is_shiny_key` ON `inventory_items`;

CREATE UNIQUE INDEX `inventory_items_user_id_resource_type_resource_id_is_shiny_key`
ON `inventory_items`(`user_id`, `resource_type`, `resource_id`, `is_shiny`);
