CREATE TABLE IF NOT EXISTS users (
  id             VARCHAR(36)  NOT NULL,
  name           VARCHAR(150) NOT NULL,
  email          VARCHAR(191) NOT NULL,
  password_hash  VARCHAR(60)  NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (id),
  CONSTRAINT uq_users_email UNIQUE (email)
)
