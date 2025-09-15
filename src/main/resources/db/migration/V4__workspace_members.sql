CREATE TABLE IF NOT EXISTS workspace_members (
                                                 id VARCHAR(36) NOT NULL,
    workspace_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_workspace_members PRIMARY KEY (id),
    CONSTRAINT uq_workspace_user UNIQUE (workspace_id, user_id),
    CONSTRAINT fk_member_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
