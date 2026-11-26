-- Unified schedule module: category + item tables.
-- No legacy plan/todo tables exist; create new structure directly.

CREATE TABLE sys_schedule_category (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  color VARCHAR(32) NOT NULL DEFAULT '#8b6df0',
  item_type VARCHAR(20) NOT NULL COMMENT 'PLAN / TASK / ALL',
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1=active 0=disabled',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_schedule_category_status_type (status, item_type, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE sys_schedule_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  item_type VARCHAR(20) NOT NULL COMMENT 'PLAN / TASK',
  description TEXT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'TODO' COMMENT 'TODO / IN_PROGRESS / COMPLETED / CANCELLED',
  priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT 'LOW / MEDIUM / HIGH',
  start_at DATETIME NULL,
  end_at DATETIME NULL,
  due_at DATETIME NULL,
  all_day TINYINT NOT NULL DEFAULT 0,
  category_id BIGINT NULL,
  related_plan_id BIGINT NULL,
  location VARCHAR(255) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  -- Timeline month queries filter deleted first, then type/status, then date overlap.
  KEY idx_item_deleted_type_status (deleted, item_type, status),
  -- Range overlap: start_at <= queryEnd AND end_at >= queryStart (plus null-safe SQL).
  KEY idx_item_deleted_start_end (deleted, start_at, end_at),
  -- Due-only tasks appear on due_at within the query window.
  KEY idx_item_deleted_due (deleted, due_at),
  -- Priority / category filters compose with deleted.
  KEY idx_item_deleted_priority (deleted, priority),
  KEY idx_item_deleted_category (deleted, category_id),
  -- Unlink related tasks when a plan is soft-deleted.
  KEY idx_item_related_plan (related_plan_id, deleted),
  CONSTRAINT fk_schedule_item_category
    FOREIGN KEY (category_id) REFERENCES sys_schedule_category (id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO sys_schedule_category (name, color, item_type, sort_order, status) VALUES
('工作', '#8b6df0', 'ALL', 10, 1),
('学习', '#5b8def', 'ALL', 20, 1),
('生活', '#48bfa4', 'ALL', 30, 1),
('健康', '#eaa66d', 'ALL', 40, 1),
('其他', '#a49db9', 'ALL', 50, 1);
