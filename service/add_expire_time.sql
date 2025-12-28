-- 为 order_info 表添加 expire_time 字段
-- 如果字段已存在，此语句不会报错

USE `ry-vue`;

-- 检查并添加 expire_time 字段
ALTER TABLE `order_info` 
ADD COLUMN IF NOT EXISTS `expire_time` datetime DEFAULT NULL COMMENT '订单过期时间' AFTER `user_id`;

-- 验证字段是否添加成功
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'ry-vue' 
  AND TABLE_NAME = 'order_info' 
  AND COLUMN_NAME = 'expire_time';
