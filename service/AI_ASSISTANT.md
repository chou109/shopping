# 逛逛（Gogo）AI 客服

逛逛由后端调用 DeepSeek 的 OpenAI 兼容 Chat Completions API，保留 Spring Boot 2.5.14，不引入 Spring AI 2.x。API Key 只从服务端运行环境读取，不要放进前端环境文件或提交到仓库。

在运行后端的本机环境设置 `DEEPSEEK_API_KEY` 后即可启用真实模型；未设置时客服安全降级并提示转人工。可选配置如下：

- `DEEPSEEK_BASE_URL`：默认 `https://api.deepseek.com`。
- `DEEPSEEK_MODEL`：默认 `deepseek-flash`，也可按 DeepSeek 账号支持的模型设置。
- `DEEPSEEK_CONNECT_TIMEOUT_MS`、`DEEPSEEK_READ_TIMEOUT_MS`：连接和读取超时。
- `DEEPSEEK_INPUT_PRICE_PER_MILLION`、`DEEPSEEK_OUTPUT_PRICE_PER_MILLION`：按账号实际价格配置，仅用于估算调用成本；默认 0。

知识检索默认使用本地 BM25 与问法归一化。需要启用语义向量检索时，可另行配置 OpenAI 兼容的 Embeddings 服务（与 DeepSeek 聊天密钥分开）：

- `AI_EMBEDDING_API_KEY`、`AI_EMBEDDING_BASE_URL`、`AI_EMBEDDING_MODEL`：三项均配置后启用；`BASE_URL` 填 API 根地址，客户端会请求 `/embeddings`。
- `AI_EMBEDDING_MINIMUM_SIMILARITY`：语义召回阈值，默认 `0.52`，范围建议 `0.4` 到 `0.8`，按所用向量模型评估集调整。
- `AI_EMBEDDING_CONNECT_TIMEOUT_MS`、`AI_EMBEDDING_READ_TIMEOUT_MS`：向量服务超时配置。

语义向量按模型和知识内容摘要缓存在后端进程内（最多 512 条）；执行 `ai_knowledge.sql` 后，FAQ、商品和长文档切片向量也会持久复用，模型标识变化或内容更新会生成新缓存。长文档正文最多 30,000 字，检索时按段落切为不超过 1,200 字的片段，最多参与 100 个长文档片段候选。用户问题向量只在进程内缓存，不会写数据库。向量服务未配置、超时或返回异常时，助手自动退回 BM25 检索；不会影响 DeepSeek 对话服务。当前对话问题和启用知识候选会发送到你配置的 Embeddings 服务；请只配置你允许处理这些内容的服务。DeepSeek 当前公开 API 文档未列出 Embeddings 端点，因此不要把 `DEEPSEEK_BASE_URL` 当作 Embeddings 服务地址。

登录用户的逛逛对话可持久化并在重新打开聊天页后恢复。首次启用前执行 `service/ai_chat_history.sql` 创建独立会话与质量指标表；升级旧指标表也可重跑该幂等脚本，补充转人工统计列。未执行时聊天仍可工作，但历史只保留在当前 WebSocket 连接中，指标页回退显示本进程数据。匿名会话不写入数据库。聊天页“清空”会删除当前登录用户当前会话的消息和会话记录；登录会话默认保留 30 天，可通过 `AI_CHAT_HISTORY_RETENTION_DAYS` 配置，过期数据在后续会话请求时清理。管理员指标页汇总近 30 天请求量、检索命中、工具成功率、转人工率、响应时间、Token 用量与估算费用；模型发起但被协议校验拦下的无效、重复或超限工具请求会计为失败。指标事件不保存问题文本或用户身份，默认保留 90 天，可通过 `AI_METRICS_RETENTION_DAYS` 配置。

每次模型请求最多使用最近 12 条有效历史消息；后端只接受 `user`/`assistant` 纯文本角色，并将单条历史限制在 4,000 字符。存储或调用链路出现额外角色和工具字段时会丢弃，不能通过恢复历史伪造系统指令或模型工具调用。

首次启用知识库前，在当前购物应用数据库执行 [`ai_knowledge.sql`](ai_knowledge.sql)。脚本会创建 FAQ 表、幂等补齐启停/来源/版本/更新时间字段、插入缺失的基础配送/售后条目，并幂等增加管理员知识库菜单与权限，不会覆盖已有答案。管理员可在后台“逛逛知识库”维护 FAQ、商城政策和商品说明，设置商品范围、优先级并启停条目；停用条目不会进入模型检索，编辑或切换状态会递增条目版本。接口通过 `/system/ai/knowledge/list`、`POST/PUT/DELETE /system/ai/knowledge` 提供读写，`PUT /system/ai/knowledge/{id}/status?enabled=0|1` 用于启停，通过 `/system/ai/knowledge/metrics` 查看近 30 天持久指标（未初始化指标表时回退本进程累计值）。接口需要 `system:ai:knowledge:list` / `system:ai:knowledge:edit` 权限或管理员角色。

检索对启用 FAQ 和在售商品使用中文词项 BM25 与问法归一化；单独配置 Embeddings 服务后启用语义召回并混合排序，向量不可用时自动回退词项检索。最多将 5 条资料及其名称作为引用传给前端。客服仅提供商品搜索和“我的订单”只读工具。订单工具从已验签且仍在 Redis 会话中的令牌取得用户 ID，忽略模型提供的用户 ID/订单号，并仅返回脱敏订单尾号和状态。退款、取消、下单等变更不开放给模型，继续由商城现有页面流程处理。

每次模型响应的工具调用都必须包含非空且不重复的调用 ID、`function` 类型和工具名，ID 最长 128 字符，参数最长 2,048 字符且必须是 JSON 对象。每个会话请求最多执行 8 次工具调用、单次模型响应最多 5 次；后端按规范化后的参数识别同一工具的重复调用，重复调用 ID 或参数等价的重复请求会停止当前回答并转人工，不会再次执行查询。

系统提示由 `GogoPromptFactory` 的 `gogo-system-v1` 生成。检索资料被作为不可信数据包裹，转义结构标记并限制每条资料长度；用户消息和检索资料中的文本都不能授权工具或改变后端权限。

没有配置 API Key 时只验证离线逻辑，不能视为真实模型链路已联通。登录用户的历史和质量指标持久化要求先执行 `service/ai_chat_history.sql`；匿名会话不持久化。

DeepSeek API 参考：[Chat Completions](https://api-docs.deepseek.com/api/create-chat-completion/) 和 [模型与价格](https://api-docs.deepseek.com/quick_start/pricing/)。
