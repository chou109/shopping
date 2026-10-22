-- Add persistent per-user comment likes before deploying the backend/frontend changes.
-- Existing like_count values remain as historical aggregate counts; there is no safe way
-- to infer which users created those legacy likes.
CREATE TABLE IF NOT EXISTS `product_comment_like` (
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`comment_id`, `user_id`),
  KEY `idx_product_comment_like_user` (`user_id`, `comment_id`),
  CONSTRAINT `fk_product_comment_like_comment`
    FOREIGN KEY (`comment_id`) REFERENCES `product_comment` (`comment_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评论点赞关系';
