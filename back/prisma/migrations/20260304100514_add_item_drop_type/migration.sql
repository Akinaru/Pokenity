-- AlterTable
ALTER TABLE `box_entries` MODIFY `resource_type` ENUM('POKEMON', 'ITEM', 'MACHINE') NOT NULL;

-- AlterTable
ALTER TABLE `inventory_items` MODIFY `resource_type` ENUM('POKEMON', 'ITEM', 'MACHINE') NOT NULL;
