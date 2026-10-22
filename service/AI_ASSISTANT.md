# 商城 AI 客服

客服通过后端直连智谱 OpenAI 兼容接口，保留 Spring Boot 2.5.14，不引入要求 Spring Boot 4.x 的 Spring AI 2.x。模型凭据只能放在后端环境变量中：

- `ZHIPU_API_KEY`：智谱 API 密钥；未配置时客服会安全降级并提示转人工。
- `ZHIPU_BASE_URL`：默认 `https://open.bigmodel.cn/api/paas/v4`。
- `ZHIPU_MODEL`：默认 `glm-5.2`。
- `ZHIPU_CONNECT_TIMEOUT_MS`、`ZHIPU_READ_TIMEOUT_MS`：连接与读取超时。
- `ZHIPU_INPUT_PRICE_PER_MILLION`、`ZHIPU_OUTPUT_PRICE_PER_MILLION`：按账户实际价格配置，用于估算调用成本；默认 0。

首次启用知识库前，在当前购物应用数据库执行 [`ai_knowledge.sql`](ai_knowledge.sql)。脚本只创建 FAQ 表并插入缺失的基础配送/售后条目，不会覆盖已有答案。管理员可通过 `/system/ai/knowledge/list`、`POST/PUT/DELETE /system/ai/knowledge` 管理问题、答案、商品范围和优先级；`/system/ai/knowledge/metrics` 查看当前进程统计。接口需要 `system:ai:knowledge:list` / `system:ai:knowledge:edit` 权限或管理员角色。

当前检索对启用 FAQ 和在售商品做中文词项匹配，最多将 5 条资料及其名称作为引用传给前端；这属于轻量关键词 RAG，尚未使用向量数据库或 embedding。客服仅提供商品搜索和“我的订单”只读工具。订单工具从已验签且仍在 Redis 会话中的令牌取得用户 ID，忽略模型提供的用户 ID/订单号，并仅返回脱敏订单尾号和状态。退款、取消、下单等变更不开放给模型，继续由商城现有页面流程处理。

后端会话历史保存在当前 WebSocket 生命周期内，最多保留 6 轮问答；质量统计为进程内累计值，重启后清零。真实模型链路需要配置有效的 `ZHIPU_API_KEY` 后才能做联网验证。
