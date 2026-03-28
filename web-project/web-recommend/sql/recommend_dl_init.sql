CREATE TABLE IF NOT EXISTS news_embedding (
  news_id BIGINT NOT NULL COMMENT '新闻ID',
  vec MEDIUMTEXT NOT NULL COMMENT '新闻向量(JSON数组)',
  dim INT NOT NULL COMMENT '向量维度',
  model_name VARCHAR(120) NOT NULL COMMENT '模型名称',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (news_id),
  KEY idx_news_embedding_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻向量表';

CREATE TABLE IF NOT EXISTS user_profile (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  vec MEDIUMTEXT NOT NULL COMMENT '用户兴趣向量(JSON数组)',
  dim INT NOT NULL COMMENT '向量维度',
  behavior_count INT NOT NULL DEFAULT 0 COMMENT '参与建模的行为数',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (user_id),
  KEY idx_user_profile_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户兴趣向量表';

CREATE TABLE IF NOT EXISTS user_recommend (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  news_id BIGINT NOT NULL COMMENT '新闻ID',
  score DOUBLE NOT NULL COMMENT '推荐分',
  rank_no INT NOT NULL COMMENT '推荐位次(1-N)',
  reason VARCHAR(50) DEFAULT NULL COMMENT '推荐来源:personalized/cold_start',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_recommend_user_news (user_id, news_id),
  KEY idx_user_recommend_user_rank (user_id, rank_no),
  KEY idx_user_recommend_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户个性化推荐结果表';
