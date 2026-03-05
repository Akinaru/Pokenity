-- AlterTable
ALTER TABLE `users`
ADD COLUMN `xp` INTEGER NOT NULL DEFAULT 0;

-- CreateTable
CREATE TABLE `box_openings` (
    `id` CHAR(36) NOT NULL,
    `user_id` CHAR(36) NOT NULL,
    `box_id` CHAR(36) NULL,
    `box_name` VARCHAR(100) NOT NULL,
    `box_pokeball_image` VARCHAR(512) NOT NULL,
    `resource_type` ENUM('POKEMON', 'ITEM', 'MACHINE') NOT NULL,
    `resource_id` INTEGER NOT NULL,
    `resource_name` VARCHAR(120) NOT NULL,
    `drop_rate` DOUBLE NOT NULL,
    `details` JSON NULL,
    `opened_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,

    INDEX `box_openings_user_id_opened_at_idx`(`user_id`, `opened_at`),
    INDEX `box_openings_box_id_idx`(`box_id`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `trades` (
    `id` CHAR(36) NOT NULL,
    `proposer_id` CHAR(36) NOT NULL,
    `recipient_id` CHAR(36) NULL,
    `status` ENUM('PENDING', 'WAITING_CONFIRMATION', 'COMPLETED', 'CANCELED', 'DECLINED') NOT NULL DEFAULT 'PENDING',
    `offered_resource_type` ENUM('POKEMON', 'ITEM', 'MACHINE') NOT NULL,
    `offered_resource_id` INTEGER NOT NULL,
    `offered_resource_name` VARCHAR(120) NOT NULL,
    `received_resource_type` ENUM('POKEMON', 'ITEM', 'MACHINE') NULL,
    `received_resource_id` INTEGER NULL,
    `received_resource_name` VARCHAR(120) NULL,
    `accepted_at` DATETIME(3) NULL,
    `confirmed_at` DATETIME(3) NULL,
    `completed_at` DATETIME(3) NULL,
    `canceled_at` DATETIME(3) NULL,
    `declined_at` DATETIME(3) NULL,
    `expires_at` DATETIME(3) NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,

    INDEX `trades_proposer_id_status_idx`(`proposer_id`, `status`),
    INDEX `trades_recipient_id_status_idx`(`recipient_id`, `status`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `box_openings` ADD CONSTRAINT `box_openings_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `box_openings` ADD CONSTRAINT `box_openings_box_id_fkey` FOREIGN KEY (`box_id`) REFERENCES `boxes`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `trades` ADD CONSTRAINT `trades_proposer_id_fkey` FOREIGN KEY (`proposer_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `trades` ADD CONSTRAINT `trades_recipient_id_fkey` FOREIGN KEY (`recipient_id`) REFERENCES `users`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;
