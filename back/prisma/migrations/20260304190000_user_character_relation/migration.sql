ALTER TABLE `users`
ADD COLUMN `character_id` CHAR(36) NULL;

CREATE INDEX `users_character_id_idx` ON `users`(`character_id`);

ALTER TABLE `users`
ADD CONSTRAINT `users_character_id_fkey`
FOREIGN KEY (`character_id`) REFERENCES `characters`(`id`)
ON DELETE SET NULL ON UPDATE CASCADE;
