-- CreateTable
CREATE TABLE `trade_requested_items` (
    `id` CHAR(36) NOT NULL,
    `trade_id` CHAR(36) NOT NULL,
    `resource_type` ENUM('POKEMON', 'ITEM', 'MACHINE') NOT NULL DEFAULT 'POKEMON',
    `resource_id` INTEGER NOT NULL,
    `resource_name` VARCHAR(120) NOT NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    INDEX `trade_requested_items_trade_id_idx`(`trade_id`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `trade_requested_items` ADD CONSTRAINT `trade_requested_items_trade_id_fkey` FOREIGN KEY (`trade_id`) REFERENCES `trades`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
