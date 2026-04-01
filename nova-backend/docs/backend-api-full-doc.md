# NovaLeap 后端接口总整理

生成时间：2026-03-29  
整理范围：`nova-backend/src/main/java/com/novaleap/api/controller` 当前已落地接口  
统计口径：仅统计 HTTP 接口，不包含预留但未实现的 WebSocket 消息通道  
当前接口总数：`76`

---

## 1. 文档用途

这份文档用于统一整理 NovaLeap 当前后端的：

- 全部接口
- 每个接口对应的功能描述
- 典型业务场景
- 鉴权方式
- 关键入参
- 返回要点
- 关键业务规则

它可以同时用作：

- 前后端联调接口总表
- 项目业务说明材料
- 面试讲项目时的模块说明底稿
- 后续补 Swagger / OpenAPI 的人工基线文档

---

## 2. 通用约定

### 2.1 统一返回结构

除 AI 流式接口外，当前接口统一返回：

```json
{
  "code": 200,
  "msg": "成功",
  "data": {}
}
```

字段说明：

- `code`：业务码，成功固定为 `200`
- `msg`：业务提示信息
- `data`：实际返回数据

### 2.2 分页结构

分页接口统一使用 MyBatis-Plus `Page<T>`，常见字段包括：

- `records`：当前页数据
- `total`：总条数
- `current`：当前页码
- `size`：每页条数

### 2.3 认证方式

当前系统采用 JWT 认证，典型请求头：

```http
Authorization: Bearer <token>
```

鉴权级别说明：

| 级别 | 说明 |
|---|---|
| `公开` | 不需要登录 |
| `已登录` | 需要 JWT，普通用户或游客都可能通过 Security |
| `注册用户` | 需要真实数据库用户，游客账号被业务层拒绝 |
| `管理员` | 需要 `ROLE_ADMIN` |

### 2.4 SSE 流式接口格式

AI 相关流式接口返回 `SseEmitter`，事件名固定为 `message`，事件数据为 JSON 字符串：

```json
{"type":"delta","content":"单次增量内容"}
```

结束帧：

```json
{"type":"done"}
```

### 2.5 关键状态值字典

#### 笔记状态 `note.status`

- `0`：待审核
- `1`：审核通过
- `2`：审核拒绝

#### 许愿状态 `wish.status`

- `0`：待审核
- `1`：审核通过
- `2`：审核拒绝

#### 自定义题库状态 `custom_question_bank.status`

- `0`：待审核
- `1`：审核通过
- `2`：审核拒绝

#### 题目状态 `question.status`

- `0`：禁用
- `1`：启用

#### 题目难度 `question.difficulty`

- `1`：简单
- `2`：中等
- `3`：困难

#### 题目来源 `question.sourceType`

- `OFFICIAL`：官方题库
- `CUSTOM`：自定义题库

---

## 3. 模块接口总览

| 模块 | 基础路径 | 接口数 | 模块职责 |
|---|---|---:|---|
| 认证与账户 | `/api/auth` | 8 | 登录、注册、游客模式、个人资料、签到 |
| AI 能力 | `/api/ai` | 7 | 题解、AI 面试教练、简历分析、笔记总结 |
| 题目系统 | `/api/questions` | 6 | 官方题库浏览、筛选、随机抽题、答案查看 |
| 自定义题库 | `/api/question-banks` | 3 | 用户导入和管理自己的题库 |
| 笔记系统 | `/api/notes` | 10 | 笔记发布、浏览、点赞、评论、删除 |
| 许愿墙 | `/api/wishes` | 5 | 愿望发布、点赞、评论、展示 |
| 排行榜 | `/api/leaderboard` | 4 | 成长积分榜、刷题打点、游戏分数打点 |
| 访问统计 | `/api/analytics` | 1 | PV/UV、访客画像、地域统计 |
| 管理后台总览 | `/api/admin` | 3 | 仪表盘、系统监控、访客记录 |
| 后台用户管理 | `/api/admin/users` | 5 | 用户 CRUD |
| 后台题目管理 | `/api/admin/questions` | 5 | 题目 CRUD |
| 后台题目分类 | `/api/admin/question-categories` | 2 | 分类查询与创建 |
| 后台笔记管理 | `/api/admin/notes` | 6 | 笔记查询、审核、管理 |
| 后台许愿管理 | `/api/admin/wishes` | 9 | 许愿审核、死信重试、队列监控 |
| 后台题库审核 | `/api/admin/question-banks` | 2 | 自定义题库审核与导入 |

---

## 4. 认证与账户模块 `auth`

模块定位：

- 负责正式用户登录注册
- 提供游客模式
- 负责个人资料读取与修改
- 提供签到连续天数信息

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| POST | `/api/auth/login` | 公开 | 用户名密码登录并签发 JWT | 正式账号登录 | `username` `password` `turnstileToken` | `token`、`user`、`avatar`、`checkin` |
| POST | `/api/auth/guest` | 公开 | 创建游客登录态 | 不想注册先体验系统 | 无 | `token`、游客 `user`、默认 `avatar`、`checkin` |
| POST | `/api/auth/register` | 公开 | 注册正式账号并自动登录 | 新用户注册 | `username` `password` `confirmPassword` `nickname` `consent` `turnstileToken` | 与登录返回一致 |
| POST | `/api/auth/logout` | 已登录 | 前端登出占位接口 | 前端清除本地 token 前调用 | 无 | 空成功结果 |
| POST | `/api/auth/password/reset` | 禁止访问 | 密码重置当前未开放 | 防止误用 | 任意 | 永远拒绝 |
| GET | `/api/auth/profile` | 已登录 | 获取当前用户资料 | 个人中心初始化 | 无 | 用户资料对象 |
| PUT | `/api/auth/profile` | 已登录 | 更新昵称、密码、头像 | 个人资料编辑 | `nickname` `password` `avatar` | 更新后的资料 |
| GET | `/api/auth/streak` | 已登录 | 获取连续签到信息 | 首页、个人中心展示活跃度 | 无 | `streakDays`、`signedToday` |

### 关键请求体

#### `POST /api/auth/login`

```json
{
  "username": "alice",
  "password": "123456",
  "turnstileToken": "optional"
}
```

#### `POST /api/auth/register`

```json
{
  "username": "alice",
  "password": "StrongPassword",
  "confirmPassword": "StrongPassword",
  "nickname": "Alice",
  "consent": true,
  "turnstileToken": "optional"
}
```

#### `PUT /api/auth/profile`

```json
{
  "nickname": "新昵称",
  "password": "NewStrongPassword",
  "avatar": "A1"
}
```

### 业务规则

- 登录接口带有登录频控和失败锁定逻辑。
- 登录、注册都支持 Turnstile 校验，但是否启用取决于配置。
- 游客登录会发放 JWT，但角色为 `GUEST`。
- 游客账号不能修改个人资料。
- `checkin` 返回结构固定包含：
  - `streakDays`
  - `signedToday`
- `logout` 只是前端交互接口，当前后端没有服务端 token 黑名单。
- `password/reset` 当前是明确关闭状态，不是“未实现但可调用”。

---

## 5. AI 能力模块 `ai`

模块定位：

- 负责 AI 题目解析
- 负责 AI 面试教练对话
- 负责简历分析
- 负责笔记摘要生成

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/ai/question/{id}/explain` | 公开 | 流式输出题目解析 | 用户点“AI 讲解题目” | 路径 `id` | SSE，逐字返回解析内容 |
| POST | `/api/ai/coach/chat` | 已登录 | 流式 AI 面试教练对话 | 面试问答、成长辅导、识图问答 | `message` `topic` `image` `mode` | SSE，逐字返回回答 |
| GET | `/api/ai/coach/history` | 已登录 | 查询教练历史对话 | 会话恢复 | `limit` | 历史消息列表 |
| POST | `/api/ai/coach/session/new` | 已登录 | 创建新的对话会话 ID | 切换新会话 | 无 | `sessionId` |
| DELETE | `/api/ai/coach/history` | 已登录 | 清空对话历史 | 用户主动清理上下文 | 无 | 空成功结果 |
| POST | `/api/ai/resume/analyze` | 已登录 | 流式简历分析 | 简历优化、岗位匹配 | `resumeText` `targetRole` | SSE，逐字返回分析 |
| POST | `/api/ai/notes/summarize` | 已登录 | 生成笔记摘要建议 | 发笔记前自动生成摘要 | `title` `content` | `List<String>`，最多 3 条摘要点 |

### 关键请求体

#### `POST /api/ai/coach/chat`

```json
{
  "message": "请模拟 Java 后端面试官问我 Redis",
  "topic": "Java后端面试",
  "image": "base64-image-optional",
  "mode": "coach"
}
```

#### `POST /api/ai/resume/analyze`

```json
{
  "resumeText": "这里是一整份简历文本",
  "targetRole": "Java Backend Engineer"
}
```

#### `POST /api/ai/notes/summarize`

```json
{
  "title": "Redis 持久化",
  "content": "详细笔记正文"
}
```

### 业务规则

- `question explain` 是唯一完全公开的 AI 接口，未登录时用客户端 IP 作为限流身份。
- `coach/chat`、`resume/analyze`、`notes/summarize` 都要求带 JWT。
- 游客账号也能使用 AI 对话与简历分析，因为它们只要求“已登录”，不要求真实数据库用户。
- AI 对话会写入对话历史，历史项字段包含：
  - `role`
  - `content`
  - `mode`
  - `topic`
  - `sessionId`
  - `timestamp`
- `coach/session/new` 返回 `sessionId`，用于前端切换上下文。
- SSE 返回事件名固定为 `message`，内容由 `delta` 和 `done` 两类帧组成。
- AI 能力统一走 `AiModelGateway`，包含额度检查、模型降级、失败兜底。
- `notes/summarize` 在 AI 不可用或额度不足时会回退到规则摘要，不会直接报错。

---

## 6. 题目系统模块 `question`

模块定位：

- 对外提供题目列表与详情
- 支持按分类、难度、关键词、题库筛选
- 支持随机抽题
- 支持查看标准答案

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/questions` | 公开 | 获取题目列表 | 刷题页初始化、筛选搜索 | `page` `size` `category` `difficulty` `keyword` `bankId` | `Page<QuestionListItemVO>` |
| GET | `/api/questions/categories` | 公开 | 获取题目分类列表 | 筛选器初始化 | 无 | `[{code,name}]` |
| GET | `/api/questions/{id}` | 公开 | 获取题目详情 | 题目详情页 | 路径 `id` | `QuestionDetailVO` |
| POST | `/api/questions/{id}/view` | 公开 | 增加题目浏览数 | 用户进入题目详情 | 路径 `id` | `id`、`viewCount` |
| GET | `/api/questions/random` | 公开 | 随机抽取 1 道题 | 随机刷题 | `category` `difficulty` `bankId` | `QuestionDetailVO` |
| GET | `/api/questions/{id}/answer` | 公开 | 获取题目答案 | 用户主动点击“查看答案” | 路径 `id` | `id`、`title`、`answer`、`source` |

### 返回对象要点

- `QuestionListItemVO`：
  - `id`
  - `title`
  - `difficulty`
  - `category`
  - `tags`
  - `viewCount`
  - `sourceType`

- `QuestionDetailVO`：
  - 在列表基础上多 `content`

- `QuestionAnswerVO`：
  - `id`
  - `title`
  - `answer`
  - `source`

### 业务规则

- 不传 `bankId` 时，只返回官方题库题目。
- 传 `bankId` 时，只能访问“已审核通过且属于当前用户”的自定义题库。
- 自定义题库题目对匿名用户不可见。
- `category` 支持别名扩展，例如上层聚合分类会展开为多个底层分类码。
- `answer` 查询会对官方题目答案做前缀清洗，避免把“标准答案：”这类说明直接暴露给前端。

---

## 7. 自定义题库模块 `question-bank`

模块定位：

- 用户上传题库文件
- 系统先保存题库元信息并进入待审核状态
- 后台审核通过后再真正导入题目

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/question-banks/mine` | 注册用户 | 查询我的题库 | 我的题库页 | `page` `size` `status` | `Page<CustomQuestionBankVO>` |
| POST | `/api/question-banks/import` | 注册用户 | 上传并导入题库文件 | 用户提交自定义题库 | multipart `file` + `name` `category` `difficulty` | 新建题库记录 |
| PUT | `/api/question-banks/{id}` | 注册用户 | 重命名题库 | 修改题库名称 | 路径 `id` + body `name` | 更新后的题库记录 |

### 导入文件要求

- 仅支持 `txt`
- 文件大小不超过 `5MB`
- 题目格式为“上方题目 + 下方答案”
- 不同题目块之间空一行

示例：

```text
题目：什么是 Redis 持久化？
答案：RDB 和 AOF ...

题目：什么是缓存穿透？
答案：...
```

### 业务规则

- 游客账号不能上传、查看、重命名自定义题库。
- 导入接口只做“解析并保存待审核题库”，不会立即写入正式题库表。
- 新题库创建后默认：
  - `status = 0` 待审核
  - `importedQuestionCount = 0`
- 题库审核通过前，前台题目接口不能访问其中的题目。

---

## 8. 笔记系统模块 `note`

模块定位：

- 用户发布技术笔记
- 前台展示审核通过笔记
- 支持浏览、点赞、评论、删除
- 提交或编辑时触发 AI 自动审核

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/notes` | 已登录 | 获取笔记列表 | 社区笔记页、搜索筛选 | `page` `size` `keyword` `category` | `Page<NoteListItemVO>` |
| GET | `/api/notes/mine` | 注册用户 | 获取我的笔记 | 我的投稿页 | `page` `size` `status` | `Page<NoteListItemVO>` |
| GET | `/api/notes/{id}` | 已登录 | 获取笔记详情 | 详情页 | 路径 `id` | `NoteDetailVO` |
| POST | `/api/notes/{id}/view` | 已登录 | 增加浏览量 | 打开详情页时上报 | 路径 `id` | 更新后的详情 |
| POST | `/api/notes` | 注册用户 | 创建笔记 | 发布技术笔记 | `title` `content` `summary` `category` `emoji` `author` | 新建后的详情 |
| PUT | `/api/notes/{id}` | 注册用户 | 编辑笔记 | 修改自己的笔记 | 路径 `id` + 编辑体 | 更新后的详情 |
| POST | `/api/notes/{id}/like` | 注册用户 | 点赞或取消点赞 | 社区互动 | 路径 `id` | `noteId` `liked` `likeCount` |
| GET | `/api/notes/{id}/comments` | 已登录 | 获取评论列表 | 评论区初始化 | 路径 `id` | `List<NoteCommentVO>` |
| POST | `/api/notes/{id}/comments` | 注册用户 | 新增评论 | 对笔记发表评论 | 路径 `id` + `content` | 新评论对象 |
| DELETE | `/api/notes/{id}` | 注册用户 | 删除笔记 | 删除自己的笔记 | 路径 `id` | 空成功结果 |

### 请求体说明

#### 创建 / 编辑笔记

```json
{
  "title": "Redis 持久化",
  "content": "正文",
  "summary": "摘要",
  "category": "tech",
  "emoji": "📘",
  "author": "张三"
}
```

#### 评论笔记

```json
{
  "content": "这篇讲得很清楚"
}
```

### 返回对象要点

- `NoteListItemVO` / `NoteDetailVO` 关键字段：
  - `id`
  - `title`
  - `content`（详情才有）
  - `summary`
  - `category`
  - `emoji`
  - `author`
  - `viewCount`
  - `status`
  - `rejectReason`
  - `likeCount`
  - `commentCount`
  - `likedByMe`
  - `wordCount`

- `NoteCommentVO`：
  - `id`
  - `noteId`
  - `userId`
  - `username`
  - `nickname`
  - `content`
  - `createdAt`
  - `mine`

### 业务规则

- 这个模块虽然经 Security 保护，但它默认要求“已登录”而不是“公开”。
- `GET /api/notes` 只返回审核通过的笔记。
- `GET /api/notes/{id}`：
  - 普通用户只能看审核通过的笔记
  - 作者本人和管理员可以看待审核 / 驳回笔记
- 创建或普通用户编辑笔记时，会触发 AI 自动审核：
  - 通过可能直接上架
  - 也可能进入待审核
  - 命中敏感规则会被拒绝
- 游客账号不能：
  - 创建笔记
  - 编辑笔记
  - 点赞
  - 评论
  - 删除
- 点赞接口是“切换式”语义，不是单纯加一。

---

## 9. 许愿墙模块 `wish`

模块定位：

- 用户发布愿望
- 愿望先进入待审核队列
- 审核通过后出现在许愿墙
- 支持点赞与评论

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/wishes` | 公开 | 获取审核通过的许愿墙列表 | 许愿墙首页 | `visitorId` | `List<WishWallItemVO>` |
| POST | `/api/wishes` | 注册用户 | 发布新愿望 | 用户提交心愿卡片 | `content` `city` | 空成功结果 |
| POST | `/api/wishes/{wishId}/like` | 公开/半实名 | 点赞或取消点赞 | 游客或登录用户点赞愿望 | 路径 `wishId` + 可选 `visitorId` | `wishId` `liked` `likeCount` |
| GET | `/api/wishes/{wishId}/comments` | 公开 | 获取愿望评论 | 评论区展示 | 路径 `wishId` | `List<WishCommentVO>` |
| POST | `/api/wishes/{wishId}/comments` | 注册用户 | 新增评论 | 对愿望发表评论 | 路径 `wishId` + `content` | 新评论对象 |

### 请求体说明

#### 发布愿望

```json
{
  "content": "希望拿到满意的后端 offer",
  "city": "上海"
}
```

#### 点赞愿望

```json
{
  "visitorId": "browser-uuid-xxx"
}
```

#### 评论愿望

```json
{
  "content": "祝你成功"
}
```

### 返回对象要点

- `WishWallItemVO`：
  - `id`
  - `content`
  - `emotion`
  - `color`
  - `city`
  - `posX`
  - `posY`
  - `floatSpeed`
  - `status`
  - `createdAt`
  - `likeCount`
  - `commentCount`
  - `likedByMe`

- `WishLikeVO`：
  - `wishId`
  - `liked`
  - `likeCount`

- `WishCommentVO`：
  - `id`
  - `wishId`
  - `userId`
  - `username`
  - `nickname`
  - `content`
  - `createdAt`
  - `mine`

### 业务规则

- 许愿提交后默认进入待审核状态，不会立刻展示。
- 提交成功后会异步进入 Redis 待审核队列。
- 愿望发布会给排行榜增加“许愿次数”积分。
- 愿望点赞支持两类身份：
  - 登录用户
  - 访客 `visitorId`
- 游客 JWT 账号不能点赞，也不能评论。
- 愿望评论必须是注册用户。

---

## 10. 排行榜模块 `leaderboard`

模块定位：

- 统计用户在平台内的成长行为
- 组合刷题数、许愿数、游戏分数生成排行榜

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/leaderboard` | 公开 | 获取排行榜与摘要 | 首页排行榜 | 无 | `summary` + `list` |
| GET | `/api/leaderboard/question-done` | 已登录 | 获取当前用户已完成题目 ID 列表 | 刷题页回显做题状态 | 无 | `List<Long>` |
| POST | `/api/leaderboard/question-done` | 注册用户 | 记录完成题目 | 用户完成一道题 | `questionId` | 空成功结果 |
| POST | `/api/leaderboard/game-score` | 注册用户 | 上报小游戏得分 | 游戏结束时记录分数 | `score` `roundId` `finalScore` | 空成功结果 |

### 业务规则

- 排行榜只统计 `USER` 角色，不统计游客。
- `GET /question-done`：
  - 匿名用户返回空数组
  - 游客账号也返回空数组
- `POST /question-done` 和 `POST /game-score` 只允许真实注册用户。
- 排行榜结果包含：
  - `summary.activeUsers`
  - `summary.questionDoneTotal`
  - `summary.wishTotal`
  - `summary.maxGameScore`
  - `list[*].rank`
  - `list[*].questionDone`
  - `list[*].wishCount`
  - `list[*].gameBestScore`
  - `list[*].totalScore`

---

## 11. 访问统计模块 `analytics`

模块定位：

- 记录 PV/UV
- 建立访客画像
- 统计地域分布
- 为后台访客记录页提供数据

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| POST | `/api/analytics/visit` | 公开 | 记录一次访问行为 | 页面进入时埋点 | `visitorId` `path` | 空成功结果 |

### 请求体说明

```json
{
  "visitorId": "browser-uuid-xxx",
  "path": "/questions/12"
}
```

### 业务规则

- 匿名访客会以 `visitor:<visitorId>` 作为 UV 身份。
- 游客账号会以 `guest:<username>` 作为身份。
- 正式用户会以 `user:<username>` 作为身份。
- 后台会基于 IP 解析城市和地区。
- Redis 中会累计：
  - 总 PV / 今日 PV
  - 总 UV / 今日 UV
  - 城市排行
  - 区域排行
  - 最近访问地理记录
- 访客画像还会保存：
  - `identity`
  - `actorType`
  - `displayName`
  - `username`
  - `ip`
  - `maskedIp`
  - `region`
  - `city`
  - `lastPath`
  - `firstSeenAt`
  - `lastSeenAt`
  - `visitCount`

---

## 12. 管理后台总览模块 `admin dashboard`

模块定位：

- 提供后台首页仪表盘
- 提供 JVM 运行态信息
- 提供访客记录管理视图

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/admin/dashboard` | 管理员 | 获取运营仪表盘统计 | 后台首页 | 无 | 用户、题目、笔记、愿望、访问统计等 |
| GET | `/api/admin/system-monitor` | 管理员 | 获取系统运行信息 | 运维监控页 | 无 | JVM 内存、线程数 |
| GET | `/api/admin/visitor-records` | 管理员 | 获取访客记录列表 | 后台访客管理 | `page` `size` `keyword` `actorType` | 分页访客画像记录 |

### 业务规则

- `dashboard` 聚合：
  - 用户总数
  - 注册用户数
  - 笔记总数
  - 题目总数
  - 愿望总数
  - PV / UV 统计
  - TOP 城市 / 区域
  - 最近地理访问记录
- 当前代码中 `aiCallCount` 是静态占位值，不是实时统计。
- `visitor-records` 支持按关键字和身份类型筛选。

---

## 13. 后台用户管理模块 `admin users`

模块定位：

- 后台管理用户基础信息
- 支持创建、编辑、删除用户

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/admin/users` | 管理员 | 分页查询用户列表 | 用户管理页 | `page` `size` `keyword` `role` | `Page<AdminUserVO>` |
| GET | `/api/admin/users/{id}` | 管理员 | 获取单个用户详情 | 编辑页初始化 | 路径 `id` | `AdminUserVO` |
| POST | `/api/admin/users` | 管理员 | 创建用户 | 后台新建账号 | `username` `password` `nickname` `role` | 新用户对象 |
| PUT | `/api/admin/users/{id}` | 管理员 | 更新用户 | 编辑用户资料 | 路径 `id` + 编辑体 | 更新后的用户对象 |
| DELETE | `/api/admin/users/{id}` | 管理员 | 删除用户 | 后台清理账号 | 路径 `id` | 空成功结果 |

### 业务规则

- 允许角色：
  - `ADMIN`
  - `USER`
  - `GUEST`
- 创建用户时：
  - `username` 必填
  - `password` 必填
  - `role` 为空时默认 `USER`
- 用户名唯一校验由业务层和数据库异常双重兜底。

---

## 14. 后台题目管理模块 `admin questions`

模块定位：

- 后台维护官方题库题目
- 可管理标题、内容、答案、分类、难度、状态

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/admin/questions` | 管理员 | 分页查询题目 | 题目管理页 | `page` `size` `category` `difficulty` `keyword` | `Page<AdminQuestionVO>` |
| GET | `/api/admin/questions/{id}` | 管理员 | 获取题目详情 | 编辑页初始化 | 路径 `id` | `AdminQuestionVO` |
| POST | `/api/admin/questions` | 管理员 | 新建题目 | 录入官方题目 | 题目保存体 | 新题目对象 |
| PUT | `/api/admin/questions/{id}` | 管理员 | 更新题目 | 编辑官方题目 | 路径 `id` + 保存体 | 更新后的题目对象 |
| DELETE | `/api/admin/questions/{id}` | 管理员 | 删除题目 | 后台移除题目 | 路径 `id` | 空成功结果 |

### 题目保存体

```json
{
  "title": "什么是 Redis 持久化",
  "content": "题目内容",
  "standardAnswer": "标准答案",
  "category": "java",
  "difficulty": 2,
  "tags": "redis,cache",
  "status": 1,
  "viewCount": 0
}
```

### 业务规则

- `difficulty` 只接受 `1~3`
- `status` 只接受 `0/1`
- `category` 会走统一分类规范化
- 后台创建题目时，空分类默认 `java`
- 空难度默认 `2`
- 空状态默认 `1` 启用

---

## 15. 后台题目分类模块 `admin question categories`

模块定位：

- 维护题目分类字典
- 为题目筛选、后台管理、自定义题库导入提供统一分类口径

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/admin/question-categories` | 管理员 | 获取题目分类列表 | 后台分类管理页 | 无 | `List<AdminQuestionCategoryVO>` |
| POST | `/api/admin/question-categories` | 管理员 | 创建分类 | 新增一个可用分类 | `name` `code` | 新分类对象 |

### 业务规则

- `name` 必填
- `code` 可选，不传时会基于名称生成
- 分类编码和分类名称都必须唯一

---

## 16. 后台笔记管理模块 `admin notes`

模块定位：

- 后台管理所有笔记
- 可人工创建、编辑、审核、删除

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/admin/notes` | 管理员 | 分页查询笔记 | 后台笔记管理页 | `page` `size` `keyword` `category` `status` | `Page<AdminNoteListItemVO>` |
| GET | `/api/admin/notes/{id}` | 管理员 | 获取笔记详情 | 详情/审核页 | 路径 `id` | `AdminNoteDetailVO` |
| POST | `/api/admin/notes` | 管理员 | 人工创建笔记 | 运营录入内容 | `AdminNoteSaveRequest` | 新建笔记详情 |
| PUT | `/api/admin/notes/{id}` | 管理员 | 编辑笔记 | 后台修订内容 | 路径 `id` + 保存体 | 更新后详情 |
| PUT | `/api/admin/notes/{id}/status` | 管理员 | 单独更新审核状态 | 审核笔记通过/驳回 | 路径 `id` + `status` + `reason/rejectReason` | 更新后详情 |
| DELETE | `/api/admin/notes/{id}` | 管理员 | 删除笔记 | 后台清理 | 路径 `id` | 空成功结果 |

### 业务规则

- 后台可直接指定：
  - `title`
  - `content`
  - `summary`
  - `category`
  - `emoji`
  - `author`
  - `userId`
  - `viewCount`
  - `status`
  - `rejectReason`
- 驳回状态必须提供驳回原因。
- 状态接口支持两种传参方式：
  - Query 参数传 `status` 和 `reason`
  - Body 传 `AdminNoteStatusRequest`

---

## 17. 后台许愿管理模块 `admin wishes`

模块定位：

- 管理许愿墙内容
- 监控审核队列
- 查看死信
- 对失败审核任务做重试

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/admin/wishes` | 管理员 | 分页查询愿望列表 | 许愿管理页 | `page` `size` `status` | `Page<AdminWishVO>` |
| GET | `/api/admin/wishes/{id}` | 管理员 | 获取单个愿望详情 | 审核详情页 | 路径 `id` | `AdminWishVO` |
| GET | `/api/admin/wishes/queue/stats` | 管理员 | 获取审核队列统计 | 队列监控 | 无 | 队列长度与待审 DB 数量 |
| GET | `/api/admin/wishes/dead-letter` | 管理员 | 获取死信愿望列表 | 死信处理页 | `size` | `List<AdminWishVO>` |
| POST | `/api/admin/wishes` | 管理员 | 人工创建愿望 | 运营补录 | `AdminWishSaveRequest` | 新建愿望 |
| PUT | `/api/admin/wishes/{id}` | 管理员 | 编辑愿望 | 后台修订愿望内容 | 路径 `id` + 保存体 | 更新后对象 |
| DELETE | `/api/admin/wishes/{id}` | 管理员 | 删除愿望 | 后台清理 | 路径 `id` | 空成功结果 |
| PUT | `/api/admin/wishes/{id}/status` | 管理员 | 修改愿望状态 | 人工审核 | 路径 `id` + `status` | 空成功结果 |
| POST | `/api/admin/wishes/{id}/retry` | 管理员 | 重试愿望审核任务 | 死信/卡单重试 | 路径 `id` | 空成功结果 |

### 业务规则

- `queue/stats` 返回：
  - `pendingQueueSize`
  - `processingQueueSize`
  - `deadLetterSize`
  - `pendingDbCount`
- `dead-letter` 读取 Redis 死信集合，再回查数据库愿望详情。
- `retry` 只允许对 `待审核` 愿望执行。
- `retry` 会清理：
  - dead letter 集合
  - retry key
  - processing lock
  - processing queue 残留
  - pending queue 重复项
- 然后重新把愿望放回待审核队列。

---

## 18. 后台题库审核模块 `admin question-banks`

模块定位：

- 审核用户上传的自定义题库
- 审核通过后正式导入题目

### 接口清单

| 方法 | 路径 | 鉴权 | 功能描述 | 业务场景 | 关键入参 | 返回要点 |
|---|---|---|---|---|---|---|
| GET | `/api/admin/question-banks` | 管理员 | 分页查询题库审核列表 | 题库审核页 | `page` `size` `status` `keyword` | `Page<AdminQuestionBankVO>` |
| PUT | `/api/admin/question-banks/{id}/audit` | 管理员 | 审核题库 | 通过或驳回用户上传题库 | 路径 `id` + 审核体 | 更新后的题库对象 |

### 审核体说明

```json
{
  "status": 1,
  "name": "Java 面试题库",
  "category": "java",
  "difficulty": 2,
  "rejectReason": ""
}
```

### 业务规则

- `status` 只能是：
  - `1` 审核通过
  - `2` 审核拒绝
- 不允许把审核状态改回 `0`。
- 审核拒绝必须提供 `rejectReason`。
- 当首次审核通过且尚未导入过时，会自动：
  - 解析原始题库文本
  - 批量写入 `question` 表
  - 设置 `sourceType = CUSTOM`
  - 绑定 `customBankId`
  - 设置 `ownerUserId`
- 已导入题库不能再次改成驳回。

---

## 19. 当前系统中的重要业务链路

### 19.1 正式用户登录链路

1. 前端调用 `/api/auth/login`
2. 后端做登录频控与验证码校验
3. 校验用户名密码
4. 生成 JWT
5. 返回用户信息、头像、签到信息

### 19.2 游客体验链路

1. 前端调用 `/api/auth/guest`
2. 后端生成游客身份和 JWT
3. 游客可访问部分已登录功能
4. 但不能发布笔记、评论、上传题库、提交排行榜、发布愿望

### 19.3 刷题链路

1. 调 `/api/questions` 或 `/api/questions/random`
2. 查看详情 `/api/questions/{id}`
3. 浏览量上报 `/api/questions/{id}/view`
4. 查看答案 `/api/questions/{id}/answer`
5. 完成后打点 `/api/leaderboard/question-done`
6. 可继续调用 `/api/ai/question/{id}/explain` 获取 AI 讲解

### 19.4 笔记发布链路

1. 用户调用 `/api/ai/notes/summarize` 先生成摘要建议
2. 调 `/api/notes` 发布笔记
3. 后端自动触发规则 + AI 审核
4. 审核通过后进入前台笔记列表
5. 其他用户可点赞、评论、浏览

### 19.5 自定义题库导入链路

1. 用户上传 `txt` 到 `/api/question-banks/import`
2. 后端解析文本并保存题库元信息，状态为待审核
3. 管理员在 `/api/admin/question-banks/{id}/audit` 审核
4. 通过后系统把题目真正导入 `question` 表
5. 用户之后可在 `/api/questions?bankId=xxx` 访问自己的题目

### 19.6 许愿墙链路

1. 用户调用 `/api/wishes` 提交愿望
2. 后端写入数据库，状态为待审核
3. 事务提交后写入 Redis 审核队列
4. 异步任务审核通过后出现在 `/api/wishes`
5. 前台访客或用户可点赞，注册用户可评论
6. 失败任务进入死信后可由后台重试

---

## 20. 当前没有落地成正式业务接口的预留能力

### 20.1 `/ws/**`

Security 已对 `/ws/**` 放行，但当前代码中没有实际的：

- `WebSocketMessageBrokerConfigurer`
- `@MessageMapping`
- `@SubscribeMapping`

所以它现在属于“预留路径”，不是当前可用业务接口。

### 20.2 密码重置

`/api/auth/password/reset` 有控制器方法，但安全配置明确 `denyAll`，它现在属于“占位接口”，不是可用功能。

---

## 21. 文档结论

当前 NovaLeap 后端接口体系已经形成了比较清晰的业务分层：

- `auth` 负责身份和账户
- `ai` 负责模型能力输出
- `question` 和 `question-bank` 负责刷题主链路
- `note` 和 `wish` 负责内容社区
- `leaderboard` 和 `analytics` 负责增长与统计
- `admin` 负责审核、运营和后台治理

如果后续你还要继续补，我建议下一步可以在这份文档基础上继续拆出三份子文档：

- `前台接口文档`
- `后台管理接口文档`
- `AI 能力接口与风控说明`

这样就能直接变成你项目里的正式后端文档体系。
