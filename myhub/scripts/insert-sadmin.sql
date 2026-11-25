-- Local development only: this is the BCrypt hash for raw password "sadmin".
-- The application verifies it with BCryptPasswordEncoder (strength 10).
INSERT INTO sys_user (username, password, nickname, status)
SELECT 'sadmin', '$2a$10$QJMwx24yNtkWqgwT8HOIgecNuT7FYII39k2vmeEYQuwEXmnjb312y', 'sadmin', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'sadmin');
