CREATE TABLE IF NOT EXISTS browse (
  RecordID INT NOT NULL,
  userId BIGINT NOT NULL,
  newsId BIGINT NOT NULL,
  browseTime DATETIME NOT NULL,
  PRIMARY KEY (RecordID),
  KEY idx_browse_user (userId),
  KEY idx_browse_news (newsId)
);
