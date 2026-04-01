# NovaLeap 后端重构交付文档

## 1. 重构变更说明文档

### 1.1 本次重构改了哪些模块
- auth 认证与账号安全模块
- analytics 访客埋点与访客画像模块
- ai 模型调用、Prompt、笔记摘要审核模块
- quota AI 配额与降级模块
- ai-audit AI 调用审计模块
- question-bank 自定义题库模块
- admin-user / admin-question / admin-note / admin-wish / admin-dashboard 管理端模块
- system/security 与 system/catalog 公共能力模块
- note / question / wish / leaderboard / ai 主业务入口模块

### 1.2 每个模块具体改了什么
- auth：新增 `AuthApplicationService`，并继续把 `AuthServiceImpl` 中的安全职责拆到 `AuthRateLimitSupport`、`AuthPasswordSupport`、`AuthCheckinSupport`、`AvatarSupport`、`TurnstileVerifier`。
- analytics：新增 `AnalyticsApplicationService`，并把 `AnalyticsServiceImpl` 中的 IP 地理解析和访客落库逻辑拆到 `AnalyticsGeoService`、`AnalyticsVisitStore`。
- ai：新增 `AiPromptFactory` 后，又继续把 `AiServiceImpl` 里的身份识别与笔记 AI 流程拆到 `AiIdentitySupport`、`AiNoteWorkflowSupport`。
- quota：新增 `AiQuotaPolicy`、`AiQuotaUsageSupport`，把 `AiLimitServiceImpl` 从硬编码 Redis 逻辑改为“策略 + 用量”双层结构。
- ai-audit：新增 `AiCallAuditService`，由 `AiModelGateway` 统一记录 AI 成功/失败调用审计。
- question-bank：新增 `QuestionBankApplicationService`，把导入、重命名、权限判断统一下沉。
- admin-*：统一补齐应用服务层，把管理端审核、状态流转、列表查询从控制器移出。
- system/security：统一 `CurrentUserService`、`CurrentUser`、`ActorIdentity`。
- system/catalog：统一 `QuestionCategoryCatalog`，收口分类别名与筛选规则。
- common/exception：`GlobalExceptionHandler` 统一真实 HTTP 状态和脱敏错误返回。

### 1.3 为什么这么改
- 解决控制器直接写业务、直连 Mapper、职责混乱的问题。
- 解决安全、配额、统计、Prompt 这些横切能力散落的问题。
- 解决 AI 项目最常见的“模型调用能用，但不可治理、不可审计、不可扩展”的问题。
- 解决后续写简历时只能讲功能、讲不出工程化架构的问题。

### 1.4 和重构前相比有哪些提升
- 主要入口已经不是“胖 Controller + 胖 Service”双重堆积，而是逐步过渡到 `controller -> application service -> support/infra`。
- 认证、安全、访客统计、AI 配额、AI 审计都有了清晰落点。
- Prompt、分类规则、当前用户上下文都完成了统一收口。
- AI 模块具备统一模型调用、统一额度检查、统一审计记录，后续扩模型和扩场景成本更低。
- 项目已经具备可维护、可扩展、可讲述的后端架构骨架。

### 1.5 是否影响现有功能
- 接口路径未改。
- 成功响应结构未主动变更。
- 错误响应更规范，HTTP 状态更准确。
- `mvn test` 已通过，13 个测试全部成功。

### 1.6 关键量化结果
控制器瘦身：
- `NoteController`：`636 -> 109`
- `QuestionController`：`369 -> 75`
- `AuthController`：`127 -> 79`
- `AnalyticsController`：`102 -> 33`
- `QuestionBankController`：`174 -> 63`
- `AdminUserController`：`187 -> 62`
- `AdminQuestionController`：`212 -> 63`
- `AdminQuestionCategoryController`：`87 -> 37`
- `AdminNoteController`：`268 -> 76`
- `AdminWishController`：`293 -> 93`
- `AdminDashboardController`：`118 -> 41`

胖 Service 收敛：
- `AuthServiceImpl`：`527 -> 263`
- `AiServiceImpl`：`454 -> 227`
- `AnalyticsServiceImpl`：`487 -> 229`
- `AiLimitServiceImpl`：`150 -> 66`

## 2. 新后端架构说明（用于简历）
NovaLeap 后端采用 `Spring Boot + MyBatis-Plus + Redis + JWT` 的单体模块化架构。整体设计不是简单地按技术层平铺，而是按业务能力和横切能力双维度拆分，使项目既能保持单体部署效率，又具备清晰的模块边界。

### 2.1 模块划分
- `auth`：登录、注册、游客模式、资料维护、签到、基础安全策略。
- `ai`：模型调用、Prompt 工厂、对话/题解/简历分析/笔记摘要与审核。
- `quota`：AI 配额、冷却、日额度、降级策略。
- `ai-audit`：AI 调用审计、成功失败记录、模型与模块级调用记录。
- `question`：题目列表、详情、抽题、答案访问。
- `question-bank`：自定义题库导入、审核导入、权限控制。
- `note`：笔记发布、审核、点赞评论、AI 总结。
- `wish`：许愿墙投递、异步审核、死信重试。
- `analytics`：访问埋点、访客画像、地域分布、后台访客记录。
- `admin`：用户、题目、分类、笔记、许愿、看板后台能力。
- `system`：当前用户上下文、分类目录、IP 解析、异常处理等公共能力。

### 2.2 分层设计
- `controller`：只负责 HTTP 协议接入、参数传递、结果返回。
- `application service`：负责业务编排、权限判断、状态流转、参数归一化。
- `support/infra`：负责 Redis、Mapper、AI Prompt、配额策略、审计记录、统计存储等基础能力。

### 2.3 AI 能力封装
- `AiModelGateway` 统一模型调用出口。
- `AiPromptFactory` 统一管理题解、聊天、简历分析、笔记总结、审核、许愿情绪分析 Prompt。
- `AiQuotaPolicy + AiQuotaUsageSupport + AiLimitServiceImpl` 统一处理配额、冷却、降级和 token 使用量。
- `AiCallAuditService` 统一记录 AI 成功/失败调用审计。
- 业务模块不再直接拼 Prompt 或直接调用底层模型客户端。

### 2.4 系统可扩展性
- 新业务优先新增模块内 `ApplicationService`，不会再把逻辑继续堆回 Controller。
- 新模型和新 Prompt 版本优先改 `ai` 模块，不影响业务入口。
- `quota` 和 `audit` 已经形成可继续扩展的独立落点，后续接成本告警、计费策略、模型灰度更顺畅。

## 3. 技术亮点总结（可直接写进简历）
- 统一认证上下文封装：抽出 `CurrentUserService`，统一处理登录用户、游客用户、管理员和数据库用户加载，解决权限判断散落和口径不一致问题。
- 控制器服务化重构：将 `note/question/auth/analytics/question-bank/admin-*` 主链路下沉到应用服务层，解决控制器臃肿、难维护、难测试问题。
- Auth 安全能力拆分：将登录限流、失败锁定、密码迁移、签到位图、头像管理、Turnstile 校验从 `AuthServiceImpl` 中拆分，解决认证服务职责过重问题。
- 题目分类规则中心化：抽出 `QuestionCategoryCatalog`，统一分类别名、筛选展开和内置分类，解决用户端、管理端、题库导入三套分类逻辑漂移问题。
- AI 调用统一封装：通过 `AiModelGateway + AiPromptFactory + AiLimitService` 收口模型调用、Prompt 管理和配额控制，解决 AI 代码散落、策略难统一问题。
- AI 配额模块化：新增 `AiQuotaPolicy` 和 `AiQuotaUsageSupport`，将额度策略和 Redis 用量存储分层，解决限流逻辑硬编码、难扩展问题。
- AI 调用审计统一落点：新增 `AiCallAuditService`，统一记录模型调用成功/失败、模块、token 消耗，解决 AI 调用不可追踪问题。
- 访客画像能力拆分：将地理位置解析和访客存储拆到 `AnalyticsGeoService`、`AnalyticsVisitStore`，解决埋点服务过度耦合问题。
- 异常与 HTTP 状态对齐：全局异常统一返回真实 HTTP 状态，并脱敏 500 错误，解决前端和监控误判成功、内部异常信息泄露问题。

## 4. 上线风险检查报告

### 4.1 已明显改善的风险
- 权限逻辑入口已统一，游客/登录/管理员口径更加一致。
- AI 调用已有统一网关、统一配额、统一审计落点，后续补限流与成本保护更容易。
- 埋点和访客画像职责已分层，统计逻辑比之前更易维护。
- 认证链路中的限流、失败锁定、验证码校验已经有清晰落点。

### 4.2 仍然存在的风险
- 默认密钥、默认账号、初始化 SQL 种子数据仍需清理。
- 全局统一限流还没有覆盖所有公网入口。
- 数据库迁移仍未引入 Flyway/Liquibase，多环境 schema 漂移风险仍在。
- AI 调用审计目前写入 Redis recent list，适合作为工程化基础，但还不是最终生产级审计表方案。
- 还没有完整的 traceId 链路追踪和 provider 级熔断策略。

### 4.3 上线结论
当前项目已经从“能跑的 AI 功能单体”提升为“有模块边界、有分层、有 AI 工程化治理意识”的后端项目，适合写进简历和面试讲解。
如果按真实生产级上线标准，还建议继续补齐以下能力：
- 配置与密钥治理
- 全局限流和风控
- 数据库迁移版本化
- traceId 与持久化审计
- provider 熔断与降级策略

## 5. 面试官视角评价

### 5.1 亮点
- 不是只改目录，而是真把主链路改成了 `controller -> application service -> support/infra`。
- AI 工程化明显优于普通练手项目，已经有统一网关、Prompt、quota、audit 落点。
- 许愿墙异步审核、题库审核导入、游客模式、访客画像这些场景有真实项目复杂度。

### 5.2 还不够强的地方
- 虽然胖 Service 已经明显收敛，但还可以继续更细粒度拆分成更明确的场景服务。
- `quota` 和 `audit` 已经有骨架，但离完整生产级平台能力还差告警、持久化、报表。
- 还没有真正接入数据库迁移和链路追踪体系。

### 5.3 面试官可能会问的问题
- 为什么选择应用服务层，而不是直接做 DDD 领域服务。
- `CurrentUserService` 与 Spring Security 的职责边界怎么划分。
- `AiQuotaPolicy` 与 `AiQuotaUsageSupport` 为什么分开设计。
- 为什么 AI 审计先落 Redis recent list，而不是直接落数据库。
- 题库审核通过后在审核服务中直接导入题目，有什么利弊。

### 5.4 建议继续加强的方向
- 引入 Flyway/Liquibase。
- 把 AI 审计从 Redis recent list 升级到持久化表。
- 补全全局限流与风控策略。
- 增加 traceId、结构化日志和告警。
- 继续抽出 `chat/resume/quota/audit` 场景级接口与管理视图。