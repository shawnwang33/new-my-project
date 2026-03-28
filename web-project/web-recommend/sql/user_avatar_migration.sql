SET @avatar_exists := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'avatar'
);
SET @add_avatar_sql := IF(@avatar_exists = 0,
  'ALTER TABLE `user` ADD COLUMN avatar VARCHAR(255) NULL COMMENT ''头像地址''',
  'SELECT 1');
PREPARE stmt_add_avatar FROM @add_avatar_sql;
EXECUTE stmt_add_avatar;
DEALLOCATE PREPARE stmt_add_avatar;

UPDATE `user`
SET avatar = 'https://dummyimage.com/64x64/e6f4ff/4c8bf5&text=U'
WHERE avatar IS NULL OR avatar = '';
