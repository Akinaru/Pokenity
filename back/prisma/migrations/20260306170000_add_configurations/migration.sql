CREATE TABLE IF NOT EXISTS `configurations` (
  `id` CHAR(36) NOT NULL,
  `key` VARCHAR(120) NOT NULL,
  `value` VARCHAR(191) NOT NULL,
  `description` VARCHAR(255) NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE INDEX `configurations_key_key`(`key`),
  PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO `configurations` (`id`, `key`, `value`, `description`)
VALUES (
  UUID(),
  'SHINY_DROP_RATE',
  '0.5',
  'Chance de drop shiny (entre 0 et 1).'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`);
