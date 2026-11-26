-- Personal HTML library: folders + files with owner isolation.

CREATE TABLE sys_library_folder (
  id BIGINT NOT NULL AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  parent_id BIGINT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_library_folder_owner_parent (owner_id, deleted, parent_id, sort_order),
  KEY idx_library_folder_parent (parent_id, deleted),
  CONSTRAINT fk_library_folder_owner FOREIGN KEY (owner_id) REFERENCES sys_user (id),
  CONSTRAINT fk_library_folder_parent FOREIGN KEY (parent_id) REFERENCES sys_library_folder (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE sys_library_file (
  id BIGINT NOT NULL AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  folder_id BIGINT NULL COMMENT 'NULL = uncategorized',
  name VARCHAR(200) NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  storage_key VARCHAR(500) NOT NULL,
  file_extension VARCHAR(10) NOT NULL,
  mime_type VARCHAR(100) NOT NULL,
  file_size BIGINT NOT NULL DEFAULT 0,
  content_hash VARCHAR(64) NOT NULL,
  is_favorite TINYINT NOT NULL DEFAULT 0,
  last_access_time DATETIME NULL,
  access_count INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_library_file_storage_key (storage_key),
  KEY idx_library_file_owner_folder (owner_id, deleted, folder_id, name),
  KEY idx_library_file_favorite (owner_id, deleted, is_favorite, update_time),
  KEY idx_library_file_recent (owner_id, deleted, last_access_time),
  CONSTRAINT fk_library_file_owner FOREIGN KEY (owner_id) REFERENCES sys_user (id),
  CONSTRAINT fk_library_file_folder FOREIGN KEY (folder_id) REFERENCES sys_library_folder (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
