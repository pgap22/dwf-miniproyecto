ALTER TABLE `data_collector`.`records` 
DROP FOREIGN KEY `records_workspaceId_fkey`;
ALTER TABLE `data_collector`.`records` 
DROP COLUMN `workspaceId`,
DROP INDEX `records_workspaceId_idx` ;
;