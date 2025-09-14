-- AlterTable
ALTER TABLE `workspace_users` ADD COLUMN `role` ENUM('OWNER', 'MEMBER') NOT NULL DEFAULT 'MEMBER';
