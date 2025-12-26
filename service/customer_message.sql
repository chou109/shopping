-- 客服消息表
-- 用于存储用户与商家客服之间的聊天消息

DROP TABLE IF EXISTS `customer_message`;
CREATE TABLE `customer_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id` varchar(64) NOT NULL COMMENT '会话ID（用于标识一个会话，格式：CS_用户ID）',
    `sender_id` bigint DEFAULT NULL COMMENT '发送者ID',
    `sender_type` tinyint NOT NULL DEFAULT 1 COMMENT '发送者类型（1-用户，2-商家）',
    `sender_name` varchar(64) DEFAULT NULL COMMENT '发送者名称',
    `sender_avatar` varchar(255) DEFAULT NULL COMMENT '发送者头像',
    `receiver_id` bigint DEFAULT NULL COMMENT '接收者ID',
    `receiver_type` tinyint NOT NULL DEFAULT 2 COMMENT '接收者类型（1-用户，2-商家）',
    `content` text NOT NULL COMMENT '消息内容',
    `message_type` tinyint NOT NULL DEFAULT 1 COMMENT '消息类型（1-文本，2-图片，3-订单卡片）',
    `order_id` bigint DEFAULT NULL COMMENT '关联订单ID（可选）',
    `product_id` bigint DEFAULT NULL COMMENT '关联商品ID（可选）',
    `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读（0-未读，1-已读）',
    `read_time` datetime DEFAULT NULL COMMENT '已读时间',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-正常，2-已撤回，3-已删除）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服消息表';

-- 添加商家端客服消息菜单
-- 请先查询现有最大菜单ID，然后根据实际情况调整下面的ID值
-- 假设消息中心菜单ID为2000，客服消息子菜单ID为2001

-- 查询现有菜单ID（执行前先运行此查询确认ID）
-- SELECT MAX(menu_id) FROM sys_menu;

-- 如果消息中心菜单已存在，只需添加子菜单
-- 请根据实际的父菜单ID调整 parent_id 的值

-- 方案一：如果你的系统没有消息中心菜单，可以执行以下语句创建
-- INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
-- VALUES ('消息中心', 0, 5, 'message', NULL, 1, 0, 'M', '0', '0', '', 'message', 'admin', NOW(), '', NULL, '消息中心目录');

-- 方案二：添加客服消息子菜单（需要替换parent_id为实际的父菜单ID）
-- 建议通过系统后台手动添加菜单，配置如下：
-- 菜单名称：客服消息
-- 路由地址：customerService
-- 组件路径：message/customerService/index
-- 权限标识：message:customerService:list
-- 菜单图标：edit

-- 插入一些测试数据（可选）
-- INSERT INTO `customer_message` (`session_id`, `sender_id`, `sender_type`, `sender_name`, `receiver_type`, `content`, `message_type`, `is_read`, `status`) VALUES
-- ('CS_1', 1, 1, '测试用户', 2, '你好，我想咨询一下商品问题', 1, 0, 1),
-- ('CS_1', NULL, 2, '客服小助手', 1, '您好！我是客服小助手，请问有什么可以帮助您的？', 1, 0, 1);
