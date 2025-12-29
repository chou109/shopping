-- 给订单表添加盲盒标识字段
ALTER TABLE order_info ADD COLUMN is_blind_box TINYINT(1) DEFAULT 0 COMMENT '是否为盲盒订单 0-否 1-是';

-- 将已有的盲盒订单（商品名称包含【盲盒】）标记为盲盒订单
UPDATE order_info oi
SET oi.is_blind_box = 1
WHERE oi.order_no IN (
    SELECT DISTINCT od.order_no 
    FROM order_detail od 
    WHERE od.product_name LIKE '%【盲盒】%'
);
