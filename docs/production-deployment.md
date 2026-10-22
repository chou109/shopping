# 生产部署配置

生产环境启用 `prod` profile 前，先在部署平台配置以下变量。数据库 URL、用户名、密码和上传目录没有代码默认值；缺少配置时应用应启动失败，避免静默连接到错误数据库或本机路径。

| 变量 | 用途 |
| --- | --- |
| `SHOPPING_DB_URL` | 完整 MySQL JDBC URL |
| `SHOPPING_DB_USERNAME` | 数据库专用账号 |
| `SHOPPING_DB_PASSWORD` | 数据库密码 |
| `RUOYI_UPLOAD_PATH` | 上传文件的持久化目录 |
| `SHOPPING_DRUID_USERNAME` | Druid 控制台账号 |
| `SHOPPING_DRUID_PASSWORD` | Druid 控制台密码 |

`SHOPPING_REDIS_HOST` 默认为 `localhost`，`SHOPPING_REDIS_PASSWORD` 默认为空；生产 Redis 若启用认证或不在本机，也需要设置相应变量。

Druid 监控台默认关闭。只有完成凭据设置并确认网络边界后，才设置 `SHOPPING_DRUID_CONSOLE_ENABLED=true`；同时用 `SHOPPING_DRUID_ALLOW` 配置允许访问的来源。来源地址会被代理拓扑影响，应在目标部署环境验证，不能只依赖应用侧白名单替代网关访问控制。

切换到环境变量前，须轮换曾经写入仓库的数据库和 Druid 凭据，并确认运行中的数据库账号具备应用所需权限。不要把真实凭据提交到仓库、日志或问题跟踪系统。生产 profile 变更后，应先在备份/预发布环境启动验证数据库连接、上传读写及监控台访问策略，再发布生产实例。

### 数据库变更

评论点赞改为持久化用户关系后，部署新版前需先备份数据库，并在预发布环境执行 `service/add_product_comment_like.sql`，确认表与外键创建成功后再部署应用。迁移是可重复执行的新增表操作；已有 `like_count` 汇总值会保留，历史点赞无法补回到具体用户。不要为了应用该变更重新导入 `sports.sql`。
