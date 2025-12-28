/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 80012
Source Host           : localhost:3306
Source Database       : sports

Target Server Type    : MYSQL
Target Server Version : 80012
File Encoding         : 65001

Date: 2025-12-27 08:16:37
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for customer_message
-- ----------------------------
DROP TABLE IF EXISTS `customer_message`;
CREATE TABLE `customer_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID（用于标识一个会话，格式：CS_用户ID）',
  `sender_id` bigint(20) DEFAULT NULL COMMENT '发送者ID',
  `sender_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '发送者类型（1-用户，2-商家）',
  `sender_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送者名称',
  `sender_avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送者头像',
  `receiver_id` bigint(20) DEFAULT NULL COMMENT '接收者ID',
  `receiver_type` tinyint(4) NOT NULL DEFAULT '2' COMMENT '接收者类型（1-用户，2-商家）',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `message_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '消息类型（1-文本，2-图片，3-订单卡片）',
  `order_id` bigint(20) DEFAULT NULL COMMENT '关联订单ID（可选）',
  `product_id` bigint(20) DEFAULT NULL COMMENT '关联商品ID（可选）',
  `is_read` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否已读（0-未读，1-已读）',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态（1-正常，2-已撤回，3-已删除）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服消息表';

-- ----------------------------
-- Records of customer_message
-- ----------------------------
INSERT INTO `customer_message` VALUES ('1', 'CS_100', '100', '1', 'aaa', null, null, '2', '如何退款？', '1', null, null, '1', '2025-12-26 17:38:04', '1', '2025-12-26 17:29:40', '2025-12-26 17:38:04');
INSERT INTO `customer_message` VALUES ('2', 'CS_100', '1', '2', 'admin', null, '100', '1', '好的，我马上为您处理。', '1', null, null, '1', '2025-12-26 17:38:17', '1', '2025-12-26 17:38:14', '2025-12-26 17:38:17');
INSERT INTO `customer_message` VALUES ('3', 'CS_100', '100', '1', 'aaa', null, null, '2', '2222', '1', null, null, '1', '2025-12-26 17:44:50', '1', '2025-12-26 17:38:44', '2025-12-26 17:44:50');
INSERT INTO `customer_message` VALUES ('4', 'CS_100', '100', '1', 'aaa', null, null, '2', '你好', '1', null, null, '1', '2025-12-26 17:44:50', '1', '2025-12-26 17:39:06', '2025-12-26 17:44:50');
INSERT INTO `customer_message` VALUES ('5', 'CS_100', '1', '2', 'admin', null, '100', '1', '你好', '1', null, null, '1', '2025-12-26 17:39:13', '1', '2025-12-26 17:39:13', '2025-12-26 17:39:13');
INSERT INTO `customer_message` VALUES ('6', 'CS_100', '100', '1', 'aaa', null, null, '2', '你是谁？', '1', null, null, '1', '2025-12-26 17:44:50', '1', '2025-12-26 17:44:16', '2025-12-26 17:44:50');
INSERT INTO `customer_message` VALUES ('7', 'CS_100', '1', '2', 'admin', null, '100', '1', '您的问题已收到，正在核实中。', '1', null, null, '1', '2025-12-26 17:48:56', '1', '2025-12-26 17:48:27', '2025-12-26 17:48:56');
INSERT INTO `customer_message` VALUES ('8', 'CS_100', '1', '2', 'admin', null, '100', '1', '好的，我马上为您处理。', '1', null, null, '1', '2025-12-26 17:52:50', '1', '2025-12-26 17:52:37', '2025-12-26 17:52:50');
INSERT INTO `customer_message` VALUES ('9', 'CS_100', '1', '2', 'admin', null, '100', '1', '好的，我马上为您处理。', '1', null, null, '1', '2025-12-26 17:52:56', '1', '2025-12-26 17:52:55', '2025-12-26 17:52:56');
INSERT INTO `customer_message` VALUES ('10', 'CS_100', '100', '1', 'aaa', null, null, '2', '22', '1', null, null, '1', '2025-12-26 20:24:33', '1', '2025-12-26 20:24:31', '2025-12-26 20:24:33');
INSERT INTO `customer_message` VALUES ('11', 'CS_100_TO_1', '100', '1', 'aaa', null, '1', '2', '如何退款？', '1', null, null, '1', '2025-12-26 21:04:42', '1', '2025-12-26 21:04:06', '2025-12-26 21:04:42');
INSERT INTO `customer_message` VALUES ('12', 'CS_100_TO_1', '1', '2', 'admin', null, null, '1', '您好，请问有什么可以帮您？', '1', null, null, '0', null, '1', '2025-12-26 21:04:45', null);
INSERT INTO `customer_message` VALUES ('13', 'CS_100_TO_100', '100', '1', 'aaa', null, '100', '2', '如何退款？', '1', null, null, '0', null, '1', '2025-12-26 21:16:00', null);
INSERT INTO `customer_message` VALUES ('14', 'CS_100_TO_100', '100', '1', 'aaa', null, '100', '2', '11', '1', null, null, '0', null, '1', '2025-12-26 21:17:31', null);
INSERT INTO `customer_message` VALUES ('15', 'CS_100_TO_100', '100', '1', 'aaa', null, '100', '2', '22', '1', null, null, '0', null, '1', '2025-12-26 21:19:14', null);
INSERT INTO `customer_message` VALUES ('16', 'CS_100_TO_100', '100', '1', 'aaa', null, '100', '2', '3', '1', null, null, '0', null, '1', '2025-12-26 21:20:22', null);
INSERT INTO `customer_message` VALUES ('17', 'CS_100_TO_2', '100', '1', 'aaa', null, '2', '2', '如何修改收货地址？', '1', null, null, '1', '2025-12-26 21:26:01', '1', '2025-12-26 21:25:58', '2025-12-26 21:26:01');
INSERT INTO `customer_message` VALUES ('18', 'CS_100_TO_2', '100', '1', 'aaa', null, '2', '2', '222', '1', null, null, '1', '2025-12-26 23:56:08', '1', '2025-12-26 21:26:20', '2025-12-26 23:56:08');
INSERT INTO `customer_message` VALUES ('19', 'CS_100_TO_1', '100', '1', 'aaa', null, '1', '2', '33', '1', null, null, '1', '2025-12-26 22:36:01', '1', '2025-12-26 21:26:41', '2025-12-26 22:36:01');
INSERT INTO `customer_message` VALUES ('20', 'CS_100_TO_37', '100', '1', 'aaa', null, '37', '2', '商品质量问题怎么处理？', '1', null, null, '0', null, '1', '2025-12-26 21:29:19', null);
INSERT INTO `customer_message` VALUES ('21', 'CS_100_TO_1', '100', '1', 'aaa', null, '1', '2', '你好', '1', null, null, '1', '2025-12-26 22:36:01', '1', '2025-12-26 22:35:34', '2025-12-26 22:36:01');
INSERT INTO `customer_message` VALUES ('22', 'CS_100_TO_1', '1', '2', 'admin', null, null, '1', '好的，我马上为您处理。', '1', null, null, '0', null, '1', '2025-12-26 23:33:17', null);
INSERT INTO `customer_message` VALUES ('23', 'CS_100_TO_1', '1', '2', 'admin', null, null, '1', '感谢您的咨询，祝您购物愉快！', '1', null, null, '0', null, '1', '2025-12-26 23:34:07', null);
INSERT INTO `customer_message` VALUES ('24', 'CS_100_TO_1', '100', '1', 'aaa', null, '1', '2', '1', '1', null, null, '1', '2025-12-26 23:52:57', '1', '2025-12-26 23:36:23', '2025-12-26 23:52:57');
INSERT INTO `customer_message` VALUES ('25', 'CS_100_TO_1', '1', '2', 'admin', null, null, '1', '好的，我马上为您处理。', '1', null, null, '0', null, '1', '2025-12-26 23:39:28', null);
INSERT INTO `customer_message` VALUES ('26', 'CS_100_TO_1', '1', '2', 'admin', null, null, '1', '您的问题已收到，正在核实中。', '1', null, null, '0', null, '1', '2025-12-26 23:39:40', null);
INSERT INTO `customer_message` VALUES ('27', 'CS_100_TO_1', '1', '2', 'admin', null, null, '1', '请稍等，我为您查询一下。', '1', null, null, '0', null, '1', '2025-12-26 23:40:07', null);
INSERT INTO `customer_message` VALUES ('28', 'CS_100_TO_1', '1', '2', 'admin', null, null, '1', '好的，我马上为您处理。', '1', null, null, '0', null, '1', '2025-12-26 23:42:04', null);
INSERT INTO `customer_message` VALUES ('29', 'CS_100_TO_1', '1', '2', 'admin', null, null, '1', '您的问题已收到，正在核实中。', '1', null, null, '0', null, '1', '2025-12-26 23:43:28', null);
INSERT INTO `customer_message` VALUES ('30', 'CS_100_TO_1', '1', '2', 'admin', null, null, '1', '请稍等，我为您查询一下。', '1', null, null, '0', null, '1', '2025-12-26 23:53:00', null);
INSERT INTO `customer_message` VALUES ('31', 'CS_100_TO_1', '1', '2', 'admin', null, null, '1', '您的问题已收到，正在核实中。', '1', null, null, '0', null, '1', '2025-12-26 23:54:41', null);
INSERT INTO `customer_message` VALUES ('32', 'CS_100_TO_1', '1', '2', 'admin', null, null, '1', '感谢您的咨询，祝您购物愉快！', '1', null, null, '0', null, '1', '2025-12-26 23:55:05', null);
INSERT INTO `customer_message` VALUES ('33', 'CS_100_TO_2', '2', '2', 'ry', null, null, '1', '好的，我马上为您处理。', '1', null, null, '1', '2025-12-27 00:05:48', '1', '2025-12-26 23:56:11', '2025-12-27 00:05:48');
INSERT INTO `customer_message` VALUES ('34', 'CS_100_TO_2', '2', '2', 'ry', null, '100', '1', '感谢您的咨询，祝您购物愉快！', '1', null, null, '1', '2025-12-27 00:05:48', '1', '2025-12-26 23:58:55', '2025-12-27 00:05:48');
INSERT INTO `customer_message` VALUES ('35', 'CS_100_TO_2', '2', '2', 'ry', null, '100', '1', '请稍等，我为您查询一下。', '1', null, null, '1', '2025-12-27 00:06:35', '1', '2025-12-27 00:06:30', '2025-12-27 00:06:35');
