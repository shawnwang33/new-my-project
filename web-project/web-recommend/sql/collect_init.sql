CREATE TABLE IF NOT EXISTS collect (
  CollectID INT NOT NULL,
  userId BIGINT NOT NULL,
  newsId BIGINT NOT NULL,
  collectTime DATETIME NOT NULL,
  PRIMARY KEY (CollectID),
  UNIQUE KEY uk_collect_user_news (userId, newsId)
);
