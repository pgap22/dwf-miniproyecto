CREATE TABLE `users` (
  `id` VARCHAR(191) NOT NULL,
  `name` VARCHAR(191) NOT NULL,
  `email` VARCHAR(191) NOT NULL,
  `password_hash` VARCHAR(191) NOT NULL,
  UNIQUE INDEX `users_email_key` (`email`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `workspaces` (
  `id` VARCHAR(191) NOT NULL,
  `name` VARCHAR(191) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `workspace_users` (
  `id` VARCHAR(191) NOT NULL,
  `workspaceId` VARCHAR(191) NOT NULL,
  `userId` VARCHAR(191) NOT NULL,
  INDEX `workspace_users_workspaceId_idx` (`workspaceId`),
  INDEX `workspace_users_userId_idx` (`userId`),
  UNIQUE INDEX `workspace_users_workspaceId_userId_key` (`workspaceId`, `userId`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `record_schemas` (
  `id` VARCHAR(191) NOT NULL,
  `name` VARCHAR(191) NOT NULL,
  `description` VARCHAR(191) NULL,
  `workspaceId` VARCHAR(191) NOT NULL,
  INDEX `record_schemas_workspaceId_idx` (`workspaceId`),
  UNIQUE INDEX `record_schemas_workspaceId_name_key` (`workspaceId`, `name`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `data_types` (
  `id` VARCHAR(191) NOT NULL,
  `name` VARCHAR(191) NOT NULL,
  `kind` ENUM('STRING','NUMBER','BOOLEAN','DATE','CATALOG') NOT NULL,
  UNIQUE INDEX `data_types_name_key` (`name`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `catalogs` (
  `id` VARCHAR(191) NOT NULL,
  `name` VARCHAR(191) NOT NULL,
  UNIQUE INDEX `catalogs_name_key` (`name`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `catalog_items` (
  `id` VARCHAR(191) NOT NULL,
  `value` VARCHAR(191) NOT NULL,
  `catalogId` VARCHAR(191) NOT NULL,
  INDEX `catalog_items_catalogId_idx` (`catalogId`),
  UNIQUE INDEX `catalog_items_catalogId_value_key` (`catalogId`, `value`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `validation_rules` (
  `id` VARCHAR(191) NOT NULL,
  `name` VARCHAR(191) NOT NULL,
  UNIQUE INDEX `validation_rules_name_key` (`name`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `record_schema_attributes` (
  `id` VARCHAR(191) NOT NULL,
  `name` VARCHAR(191) NOT NULL,
  `isRequired` TINYINT(1) NOT NULL DEFAULT 0,
  `allowMultiple` TINYINT(1) NOT NULL DEFAULT 0,
  `recordSchemaId` VARCHAR(191) NOT NULL,
  `dataTypeId` VARCHAR(191) NOT NULL,
  `catalogId` VARCHAR(191) NULL,
  INDEX `record_schema_attributes_recordSchemaId_idx` (`recordSchemaId`),
  INDEX `record_schema_attributes_dataTypeId_idx` (`dataTypeId`),
  INDEX `record_schema_attributes_catalogId_idx` (`catalogId`),
  UNIQUE INDEX `record_schema_attributes_recordSchemaId_name_key` (`recordSchemaId`, `name`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `attribute_validations` (
  `id` VARCHAR(191) NOT NULL,
  `attributeId` VARCHAR(191) NOT NULL,
  `validationRuleId` VARCHAR(191) NOT NULL,
  `validationValue` VARCHAR(191) NOT NULL,
  `isNegated` TINYINT(1) NOT NULL DEFAULT 0,
  INDEX `attribute_validations_attributeId_idx` (`attributeId`),
  INDEX `attribute_validations_validationRuleId_idx` (`validationRuleId`),
  UNIQUE INDEX `attribute_validations_attributeId_validationRuleId_validatio_key`
    (`attributeId`, `validationRuleId`, `validationValue`, `isNegated`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `records` (
  `id` VARCHAR(191) NOT NULL,
  `data` JSON NOT NULL,
  `userId` VARCHAR(191) NOT NULL,
  `workspaceId` VARCHAR(191) NOT NULL,
  `recordSchemaId` VARCHAR(191) NOT NULL,
  INDEX `records_workspaceId_idx` (`workspaceId`),
  INDEX `records_recordSchemaId_idx` (`recordSchemaId`),
  INDEX `records_userId_idx` (`userId`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `workspace_users`
  ADD CONSTRAINT `workspace_users_workspaceId_fkey`
  FOREIGN KEY (`workspaceId`) REFERENCES `workspaces` (`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE,
  ADD CONSTRAINT `workspace_users_userId_fkey`
  FOREIGN KEY (`userId`) REFERENCES `users` (`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `record_schemas`
  ADD CONSTRAINT `record_schemas_workspaceId_fkey`
  FOREIGN KEY (`workspaceId`) REFERENCES `workspaces` (`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `record_schema_attributes`
  ADD CONSTRAINT `record_schema_attributes_recordSchemaId_fkey`
  FOREIGN KEY (`recordSchemaId`) REFERENCES `record_schemas` (`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE,
  ADD CONSTRAINT `record_schema_attributes_dataTypeId_fkey`
  FOREIGN KEY (`dataTypeId`) REFERENCES `data_types` (`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE,
  ADD CONSTRAINT `record_schema_attributes_catalogId_fkey`
  FOREIGN KEY (`catalogId`) REFERENCES `catalogs` (`id`)
  ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `catalog_items`
  ADD CONSTRAINT `catalog_items_catalogId_fkey`
  FOREIGN KEY (`catalogId`) REFERENCES `catalogs` (`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `attribute_validations`
  ADD CONSTRAINT `attribute_validations_attributeId_fkey`
  FOREIGN KEY (`attributeId`) REFERENCES `record_schema_attributes` (`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE,
  ADD CONSTRAINT `attribute_validations_validationRuleId_fkey`
  FOREIGN KEY (`validationRuleId`) REFERENCES `validation_rules` (`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `records`
  ADD CONSTRAINT `records_userId_fkey`
  FOREIGN KEY (`userId`) REFERENCES `users` (`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE,
  ADD CONSTRAINT `records_workspaceId_fkey`
  FOREIGN KEY (`workspaceId`) REFERENCES `workspaces` (`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE,
  ADD CONSTRAINT `records_recordSchemaId_fkey`
  FOREIGN KEY (`recordSchemaId`) REFERENCES `record_schemas` (`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE;
