-- 商品评价功能相关数据库表

-- 评价主表
DROP TABLE IF EXISTS `mt_goods_comment`;

CREATE TABLE `mt_goods_comment` (
  `ID` int NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `MERCHANT_ID` int NOT NULL DEFAULT '0' COMMENT '商户ID',
  `STORE_ID` int NOT NULL DEFAULT '0' COMMENT '店铺ID',
  `ORDER_ID` int NOT NULL DEFAULT '0' COMMENT '订单ID',
  `GOODS_ID` int DEFAULT '0' COMMENT '商品ID',
  `SKU_ID` int DEFAULT '0' COMMENT 'SKU ID',
  `USER_ID` int NOT NULL DEFAULT '0' COMMENT '用户ID',
  `COMMENT_TYPE` tinyint NOT NULL DEFAULT '1' COMMENT '评价类型: 1-商品评价 2-订单NPS评价',
  `SCORE` tinyint NOT NULL DEFAULT '5' COMMENT '评分(1-5星或0-10分)',
  `CONTENT` varchar(1000) DEFAULT '' COMMENT '评价内容',
  `REPLY_CONTENT` varchar(1000) DEFAULT '' COMMENT '商家回复内容',
  `REPLY_TIME` datetime DEFAULT NULL COMMENT '商家回复时间',
  `IS_ANONYMOUS` char(1) DEFAULT 'N' COMMENT '是否匿名评价 Y-是 N-否',
  `IS_SHOW` char(1) DEFAULT 'Y' COMMENT '是否显示 Y-显示 N-隐藏',
  `LIKE_COUNT` int DEFAULT '0' COMMENT '点赞数',
  `CREATE_TIME` datetime DEFAULT NULL COMMENT '创建时间',
  `UPDATE_TIME` datetime DEFAULT NULL COMMENT '更新时间',
  `OPERATOR` varchar(30) DEFAULT '' COMMENT '最后操作人',
  `STATUS` char(1) DEFAULT 'A' COMMENT '状态 A-正常 D-删除',
  PRIMARY KEY (`ID`),
  KEY `idx_merchant_id` (`MERCHANT_ID`),
  KEY `idx_store_id` (`STORE_ID`),
  KEY `idx_order_id` (`ORDER_ID`),
  KEY `idx_goods_id` (`GOODS_ID`),
  KEY `idx_user_id` (`USER_ID`),
  KEY `idx_create_time` (`CREATE_TIME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- 评价图片表
DROP TABLE IF EXISTS `mt_goods_comment_image`;

CREATE TABLE `mt_goods_comment_image` (
  `ID` int NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `COMMENT_ID` int NOT NULL DEFAULT '0' COMMENT '评价ID',
  `IMAGE_URL` varchar(500) NOT NULL DEFAULT '' COMMENT '图片地址',
  `SORT` int DEFAULT '0' COMMENT '排序',
  `CREATE_TIME` datetime DEFAULT NULL COMMENT '创建时间',
  `STATUS` char(1) DEFAULT 'A' COMMENT '状态 A-正常 D-删除',
  PRIMARY KEY (`ID`),
  KEY `idx_comment_id` (`COMMENT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价图片表';
