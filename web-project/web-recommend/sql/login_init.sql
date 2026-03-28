CREATE TABLE IF NOT EXISTS `user` (
  `userId` BIGINT(20) NOT NULL COMMENT '用户编码',
  `userName` VARCHAR(100) NOT NULL COMMENT '用户名',
  `userAccount` VARCHAR(200) NOT NULL COMMENT '用户账号',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  `Introduction` VARCHAR(1000) DEFAULT NULL COMMENT '用户个人简介',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  PRIMARY KEY (`userId`),
  UNIQUE KEY `uk_user_name` (`userName`),
  UNIQUE KEY `uk_user_account` (`userAccount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

CREATE TABLE IF NOT EXISTS `admin` (
  `AdminId` BIGINT(20) NOT NULL COMMENT '管理员编码',
  `AdminName` VARCHAR(100) NOT NULL COMMENT '管理员名',
  `AdminAccount` VARCHAR(200) NOT NULL COMMENT '管理员账号',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  PRIMARY KEY (`AdminId`),
  UNIQUE KEY `uk_admin_name` (`AdminName`),
  UNIQUE KEY `uk_admin_account` (`AdminAccount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员信息表';

INSERT INTO `user` (`userId`, `userName`, `userAccount`, `avatar`, `Introduction`, `password`)
SELECT 10001, '普通用户', 'user', 'https://dummyimage.com/64x64/e6f4ff/4c8bf5&text=U', '默认用户账号', '123456'
WHERE NOT EXISTS (
    SELECT 1 FROM `user` WHERE `userAccount` = 'user'
);

INSERT INTO `admin` (`AdminId`, `AdminName`, `AdminAccount`, `password`)
SELECT 9001, '系统管理员', 'admin', '123456'
WHERE NOT EXISTS (
    SELECT 1 FROM `admin` WHERE `AdminAccount` = 'admin'
);
