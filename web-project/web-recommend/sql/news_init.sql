CREATE TABLE IF NOT EXISTS news_category (
  id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  name VARCHAR(50) NOT NULL COMMENT '分类名称',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (id),
  UNIQUE KEY uk_news_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻分类表';

CREATE TABLE IF NOT EXISTS news_article (
  id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '新闻ID',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  summary VARCHAR(600) DEFAULT NULL COMMENT '摘要',
  content TEXT COMMENT '新闻详情正文',
  cover_url VARCHAR(500) DEFAULT NULL COMMENT '封面图',
  source VARCHAR(100) DEFAULT NULL COMMENT '来源',
  category_id BIGINT(20) NOT NULL COMMENT '分类ID',
  publish_time DATETIME NOT NULL COMMENT '发布时间',
  like_count INT NOT NULL DEFAULT 0 COMMENT '点赞量',
  favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏量',
  view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  recommend_score INT NOT NULL DEFAULT 0 COMMENT '推荐分',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  PRIMARY KEY (id),
  KEY idx_news_article_category (category_id),
  KEY idx_news_article_hot (view_count),
  CONSTRAINT fk_news_article_category FOREIGN KEY (category_id) REFERENCES news_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻内容表';

-- 兼容已有数据库：若 news_article 缺少 content 字段则补齐
SET @has_content = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'news_article'
    AND column_name = 'content'
);
SET @ddl_sql = IF(@has_content = 0,
  'ALTER TABLE news_article ADD COLUMN content TEXT COMMENT ''新闻详情正文'' AFTER summary',
  'SELECT 1');
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_like_count = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'news_article'
    AND column_name = 'like_count'
);
SET @ddl_like_sql = IF(@has_like_count = 0,
  'ALTER TABLE news_article ADD COLUMN like_count INT NOT NULL DEFAULT 0 COMMENT ''点赞量'' AFTER publish_time',
  'SELECT 1');
PREPARE stmt_like FROM @ddl_like_sql;
EXECUTE stmt_like;
DEALLOCATE PREPARE stmt_like;

SET @has_favorite_count = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'news_article'
    AND column_name = 'favorite_count'
);
SET @ddl_favorite_sql = IF(@has_favorite_count = 0,
  'ALTER TABLE news_article ADD COLUMN favorite_count INT NOT NULL DEFAULT 0 COMMENT ''收藏量'' AFTER like_count',
  'SELECT 1');
PREPARE stmt_favorite FROM @ddl_favorite_sql;
EXECUTE stmt_favorite;
DEALLOCATE PREPARE stmt_favorite;

CREATE TABLE IF NOT EXISTS news_like (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  news_id BIGINT(20) NOT NULL,
  user_account VARCHAR(100) NOT NULL,
  create_time DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_news_like (news_id, user_account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻点赞关系表';

CREATE TABLE IF NOT EXISTS news_favorite (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  news_id BIGINT(20) NOT NULL,
  user_account VARCHAR(100) NOT NULL,
  create_time DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_news_favorite (news_id, user_account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻收藏关系表';

INSERT INTO news_category (id, name, sort_no)
VALUES
  (1, '时政新闻', 1),
  (2, '经济新闻', 2),
  (3, '社会新闻', 3),
  (4, '文化新闻', 4),
  (5, '体育新闻', 5),
  (6, '科技新闻', 6),
  (7, '国际新闻', 7)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_no = VALUES(sort_no);

INSERT INTO news_article (
  id, title, summary, content, cover_url, source, category_id, publish_time, like_count, favorite_count, view_count, recommend_score, status
)
VALUES
  (1001, '山东泰山队夺得中超冠军', '在本赛季关键战中，山东泰山队稳扎稳打，最终提前锁定冠军。',
   '本赛季中超联赛竞争激烈，山东泰山队在关键比赛中展现出稳定的战术执行力与强烈的求胜欲。\n\n球队在攻防两端保持平衡，多名核心球员在关键时刻挺身而出。教练组在排兵布阵上的针对性调整，也成为冲刺阶段的重要优势。\n\n随着冠军归属尘埃落定，俱乐部将围绕青训和阵容深度继续完善，为下赛季多线作战做好准备。',
   'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=640&q=80', '体育日报', 5, '2026-03-10 13:04:00', 267, 98, 5234, 97, 1),
  (1002, '量子计算研究取得新突破', '科研团队在量子纠错方向取得进展，为实用化奠定基础。',
   '量子计算作为下一代计算技术的重要方向，近年来在硬件稳定性和算法优化方面持续突破。\n\n此次研究成果主要集中在量子纠错机制与门控精度提升，为后续在材料模拟、密码分析、复杂优化等场景的应用奠定基础。\n\n业内专家认为，未来三到五年将是量子技术从实验室走向产业化验证的关键阶段。',
   'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=640&q=80', '前沿科技', 6, '2026-03-10 10:04:00', 183, 76, 4567, 95, 1),
  (1003, '多地召开高质量发展推进会', '围绕新质生产力与产业升级，多地发布年度行动计划。',
   '围绕高质量发展这一主线，多地召开专题推进会，强调实体经济升级与新兴产业培育并重。\n\n会议提出将通过科技创新、金融支持和政策协同，提升产业链供应链韧性，推动区域协调发展。\n\n在具体执行层面，各地将细化年度目标，推进重点项目落地，持续优化营商环境。',
   'https://images.unsplash.com/photo-1475721027785-f74eccf877e2?auto=format&fit=crop&w=640&q=80', '中国经济观察', 2, '2026-03-10 09:04:00', 144, 65, 4200, 91, 1),
  (1004, '数字经济加速赋能实体产业', '平台经济与制造业融合持续深入，产业链协同效率不断提升。',
   '数字经济正在以更深层次推动传统产业升级。随着算力基础设施和数据要素市场建设不断完善，企业数字化转型进入提质增效新阶段。',
   'https://images.unsplash.com/photo-1518186285589-2f7649de83e0?auto=format&fit=crop&w=640&q=80', '经济周刊', 2, '2026-03-10 08:04:00', 130, 59, 4123, 89, 1),
  (1005, '博物馆夜场活动带热城市文旅', '多地博物馆推出夜游项目，文化消费活力进一步释放。',
   '多地文博场馆结合地方特色推出夜场活动，带动文旅消费持续升温，成为夜间经济新的增长点。',
   'https://images.unsplash.com/photo-1554907984-15263bfd63bd?auto=format&fit=crop&w=640&q=80', '文化视野', 4, '2026-03-09 20:30:00', 126, 52, 3987, 87, 1),
  (1006, '城市更新项目改善社区生活', '老旧小区改造提速，公共服务与居住环境明显提升。',
   '各地城市更新项目从基础设施改造延伸到社区服务优化，群众获得感持续增强。',
   'https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=640&q=80', '社会纵横', 3, '2026-03-09 19:20:00', 118, 49, 3876, 84, 1),
  (1007, '中国选手夺得世界体操冠军', '在决赛中发挥稳定，以高难度动作赢得满堂喝彩。',
   '在本次国际赛事决赛中，中国选手发挥稳定，最终凭借高质量完成获得冠军。',
   'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&w=640&q=80', '体育速递', 5, '2026-03-09 18:10:00', 109, 44, 3456, 82, 1),
  (1008, '新能源汽车市场持续增长', '销量与充电基础设施同步增长，产业链景气度维持高位。',
   '新能源汽车在政策支持和技术升级的双重驱动下持续增长，产业链投资热度保持高位。',
   'https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&w=640&q=80', '产业观察', 2, '2026-03-09 17:00:00', 101, 42, 3420, 80, 1),
  (1009, '人工智能大模型应用加速落地', '教育、医疗、制造等领域均出现规模化应用案例。',
   '人工智能大模型正在从技术演示走向业务落地，行业应用边界不断拓展。',
   'https://images.unsplash.com/photo-1677442136019-21780ecad995?auto=format&fit=crop&w=640&q=80', 'AI周刊', 6, '2026-03-09 15:40:00', 97, 40, 3267, 79, 1),
  (1010, '多国就气候议题展开磋商', '与会代表就减排目标与绿色融资机制交换意见。',
   '在最新一轮气候议题磋商中，多方围绕减排目标、绿色融资与技术转移展开深入交流。\n\n与会代表普遍认为，应在兼顾发展权与减排责任的前提下，建立更具可操作性的国际合作机制。\n\n下一阶段，各方将继续就阶段性目标与监督评估体系进行沟通。',
   'https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=640&q=80', '国际观察', 7, '2026-03-09 12:40:00', 91, 38, 3261, 78, 1),
  (1011, '基层治理数字化转型持续推进', '通过数据平台联动，群众办事效率与响应速度显著提高。',
   '基层治理数字化转型持续深入，更多便民场景实现线上办理和跨部门协同。',
   'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=640&q=80', '民生周报', 3, '2026-03-09 10:00:00', 88, 37, 3188, 77, 1),
  (1012, '古籍修复成果展在国家图书馆开幕', '展览集中展示珍贵古籍修复流程与数字化保护成果。',
   '本次展览集中展示古籍修复与数字化保护成果，吸引大量观众参观体验。',
   'https://images.unsplash.com/photo-1513001900722-370f803f498d?auto=format&fit=crop&w=640&q=80', '文化中国', 4, '2026-03-08 18:20:00', 80, 34, 3044, 76, 1)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  summary = VALUES(summary),
  content = VALUES(content),
  cover_url = VALUES(cover_url),
  source = VALUES(source),
  category_id = VALUES(category_id),
  publish_time = VALUES(publish_time),
  like_count = VALUES(like_count),
  favorite_count = VALUES(favorite_count),
  view_count = view_count,
  recommend_score = VALUES(recommend_score),
  status = VALUES(status);

INSERT INTO news_like (news_id, user_account, create_time)
VALUES
  (1001, 'user', '2026-03-14 08:30:00'),
  (1001, 'zhangsan', '2026-03-14 08:35:00'),
  (1002, 'user', '2026-03-14 09:12:00'),
  (1003, 'user', '2026-03-14 09:50:00')
ON DUPLICATE KEY UPDATE create_time = VALUES(create_time);

INSERT INTO news_favorite (news_id, user_account, create_time)
VALUES
  (1001, 'user', '2026-03-14 08:40:00'),
  (1002, 'user', '2026-03-14 09:20:00'),
  (1003, 'zhangsan', '2026-03-14 09:58:00')
ON DUPLICATE KEY UPDATE create_time = VALUES(create_time);
