-- Run once against the shopping application database to enable the editable RAG FAQ corpus.
CREATE TABLE IF NOT EXISTS `frequently_asked_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question` VARCHAR(255) NOT NULL,
  `answer` TEXT NOT NULL,
  `product_id` BIGINT DEFAULT NULL COMMENT 'NULL means general store knowledge',
  `priority` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_faq_product_priority` (`product_id`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客服可维护知识库';

INSERT INTO `frequently_asked_question` (`question`, `answer`, `product_id`, `priority`)
SELECT '目前支持哪些支付方式？', '当前结算页提供的支付选项为准；若账户余额支付可用，可使用余额完成支付。其他支付方式以结算页实际显示为准。', NULL, 100
WHERE NOT EXISTS (SELECT 1 FROM `frequently_asked_question` WHERE `question` = '目前支持哪些支付方式？' AND `product_id` IS NULL);

INSERT INTO `frequently_asked_question` (`question`, `answer`, `product_id`, `priority`)
SELECT '如何查看订单配送进度？', '请登录后在“我的订单”查看订单状态和发货时间。若页面没有物流轨迹或时效信息，请联系店铺客服核实，不要依据客服助手的估算安排收货。', NULL, 90
WHERE NOT EXISTS (SELECT 1 FROM `frequently_asked_question` WHERE `question` = '如何查看订单配送进度？' AND `product_id` IS NULL);

INSERT INTO `frequently_asked_question` (`question`, `answer`, `product_id`, `priority`)
SELECT '如何申请退款或售后？', '请登录后进入“我的订单”，按订单页面提供的售后/退款操作提交申请。平台客服助手不会直接修改订单或承诺退款结果；具体规则和处理进度以订单页面及店铺审核为准。', NULL, 90
WHERE NOT EXISTS (SELECT 1 FROM `frequently_asked_question` WHERE `question` = '如何申请退款或售后？' AND `product_id` IS NULL);
