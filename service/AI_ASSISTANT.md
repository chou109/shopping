# 逛逛（Gogo）AI 客服

逛逛由后端调用 DeepSeek 的 OpenAI 兼容 Chat Completions API，保留 Spring Boot 2.5.14，不引入 Spring AI 2.x。API Key 只从服务端运行环境读取，不要放进前端环境文件或提交到仓库。

在运行后端的本机环境设置 `DEEPSEEK_API_KEY` 后即可启用真实模型；未设置时客服安全降级并提示转人工。可选配置如下：

- `DEEPSEEK_BASE_URL`：默认 `https://api.deepseek.com`。
- `DEEPSEEK_MODEL`：默认 `deepseek-flash`，也可按 DeepSeek 账号支持的模型设置。
- `DEEPSEEK_CONNECT_TIMEOUT_MS`、`DEEPSEEK_READ_TIMEOUT_MS`：连接和读取超时。
- `DEEPSEEK_INPUT_PRICE_PER_MILLION`、`DEEPSEEK_OUTPUT_PRICE_PER_MILLION`：按账号实际价格配置，仅用于估算调用成本；默认 0。

首次启用知识库前，在当前购物应用数据库执行 [`ai_knowledge.sql`](ai_knowledge.sql)。脚本只创建 FAQ 表、插入缺失的基础配送/售后条目，并幂等增加管理员知识库菜单与权限，不会覆盖已有答案。管理员可在后台“逛逛知识库”维护问题、答案、商品范围和优先级；接口仍通过 `/system/ai/knowledge/list`、`POST/PUT/DELETE /system/ai/knowledge` 提供读写，通过 `/system/ai/knowledge/metrics` 查看当前进程统计。接口需要 `system:ai:knowledge:list` / `system:ai:knowledge:edit` 权限或管理员角色。

当前检索对启用 FAQ 和在售商品做中文词项匹配，最多将 5 条资料及其名称作为引用传给前端；这属于轻量关键词 RAG，尚未使用向量数据库或 embedding。客服仅提供商品搜索和“我的订单”只读工具。订单工具从已验签且仍在 Redis 会话中的令牌取得用户 ID，忽略模型提供的用户 ID/订单号，并仅返回脱敏订单尾号和状态。退款、取消、下单等变更不开放给模型，继续由商城现有页面流程处理。

系统提示由 `GogoPromptFactory` 的 `gogo-system-v1` 生成。检索资料被作为不可信数据包裹，转义结构标记并限制每条资料长度；用户消息和检索资料中的文本都不能授权工具或改变后端权限。

后端会话历史保存在当前 WebSocket 生命周期内，最多保留 6 轮问答；质量统计为进程内累计值，重启后清零。没有配置 API Key 时只验证离线逻辑，不能视为真实模型链路已联通。

DeepSeek API 参考：[Chat Completions](https://api-docs.deepseek.com/api/create-chat-completion/) 和 [模型与价格](https://api-docs.deepseek.com/quick_start/pricing/)。
