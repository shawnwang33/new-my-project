CREATE TABLE IF NOT EXISTS `comment` (
  commentId INT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  userId BIGINT(20) NOT NULL COMMENT '用户ID',
  newsId BIGINT(20) NOT NULL COMMENT '新闻ID',
  commentTime DATETIME NOT NULL COMMENT '评论时间',
  commentContent VARCHAR(1000) NOT NULL COMMENT '评论内容',
  PRIMARY KEY (commentId),
  KEY idx_comment_newsId (newsId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻评论表';

-- 若存在旧外键(指向旧 news 表)，先移除，避免插入测试数据失败
SET @old_fk_exists = (
  SELECT COUNT(1)
  FROM information_schema.table_constraints
  WHERE table_schema = DATABASE()
    AND table_name = 'comment'
    AND constraint_type = 'FOREIGN KEY'
    AND constraint_name = 'fk_comment_news'
);
SET @drop_old_fk_sql = IF(@old_fk_exists = 1,
  'ALTER TABLE `comment` DROP FOREIGN KEY fk_comment_news',
  'SELECT 1');
PREPARE stmt0 FROM @drop_old_fk_sql;
EXECUTE stmt0;
DEALLOCATE PREPARE stmt0;

-- 兼容已有 comment 表，补充展示字段
SET @has_user_account = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'comment'
    AND column_name = 'userAccount'
);
SET @add_user_account_sql = IF(@has_user_account = 0,
  'ALTER TABLE `comment` ADD COLUMN userAccount VARCHAR(100) NULL COMMENT ''用户账号'' AFTER userId',
  'SELECT 1');
PREPARE stmt1 FROM @add_user_account_sql;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET @has_like_count = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'comment'
    AND column_name = 'likeCount'
);
SET @add_like_count_sql = IF(@has_like_count = 0,
  'ALTER TABLE `comment` ADD COLUMN likeCount INT NOT NULL DEFAULT 0 COMMENT ''点赞数'' AFTER commentContent',
  'SELECT 1');
PREPARE stmt2 FROM @add_like_count_sql;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

INSERT INTO `comment` (commentId, userId, userAccount, newsId, commentTime, commentContent, likeCount)
VALUES
  (1, 10001, 'user', 1001, '2026-03-14 09:20:00', '关注民生议题非常有意义，期待后续政策细化落地。', 12),
  (2, 10002, 'zhangsan', 1001, '2026-03-14 10:05:00', '医疗和教育资源均衡是重点，建议加强基层保障。', 8),
  (3, 10003, 'tech_fan', 1002, '2026-03-14 10:42:00', '量子计算进展很快，期待看到更多产业化应用。', 15),
  (4, 10001, 'user', 1003, '2026-03-14 11:18:00', '高质量发展离不开科技创新和人才支撑。', 6)
ON DUPLICATE KEY UPDATE
  userId = VALUES(userId),
  userAccount = VALUES(userAccount),
  newsId = VALUES(newsId),
  commentTime = VALUES(commentTime),
  commentContent = VALUES(commentContent),
  likeCount = VALUES(likeCount);
