-- Optional persistent history and quality metrics for Gogo (逛逛).
-- Run once against the application database; no existing shopping data is changed.
CREATE TABLE IF NOT EXISTS `ai_chat_conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `conversation_key` CHAR(36) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_chat_conversation_key` (`conversation_key`),
  KEY `idx_ai_chat_conversation_user_update` (`user_id`, `update_time`),
  KEY `idx_ai_chat_conversation_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逛逛持久会话';

CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `conversation_id` BIGINT NOT NULL,
  `role` VARCHAR(16) NOT NULL COMMENT 'user or assistant',
  `content` TEXT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_message_conversation_id` (`conversation_id`, `id`),
  CONSTRAINT `fk_ai_chat_message_conversation`
    FOREIGN KEY (`conversation_id`) REFERENCES `ai_chat_conversation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逛逛会话消息';

CREATE TABLE IF NOT EXISTS `ai_chat_metric_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `retrieval_hit` TINYINT NOT NULL DEFAULT 0,
  `tool_calls` INT NOT NULL DEFAULT 0,
  `tool_failures` INT NOT NULL DEFAULT 0,
  `duration_ms` BIGINT NOT NULL DEFAULT 0,
  `prompt_tokens` INT NOT NULL DEFAULT 0,
  `completion_tokens` INT NOT NULL DEFAULT 0,
  `estimated_cost` DECIMAL(20, 8) NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_metric_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逛逛质量指标';
