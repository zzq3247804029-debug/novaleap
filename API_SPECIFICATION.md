# NovaLeap 项目接口规范说明文档 (API & Frontend Integration)

本文档提供了 NovaLeap 平台完整的前后端接口映射指南。所有后端接口均遵循 `Result<T>` 标准响应格式，前端通过 `axios` 进行调用并处理状态。

---

## 🏗 全局约定

-   **Base URL**: `http://localhost:8080` (后端) / `http://localhost:80` (前端 Nginx)
-   **认证方式**: 请求头携带 `Authorization: Bearer <TOKEN>`
-   **标准响应格式**:
    ```json
    {
      "code": 200,      // 200 为成功，其他为错误码
      "message": "success",
      "data": { ... }   // 业务数据
    }
    ```

---

## 1. 认证与用户模块 (Auth & User)
**后端控制器**: `AuthController.java` | **对应前端**: `Login.vue`, `ProfileView.vue`

| 接口路径 | 方法 | 说明 | 参数 / Body |
| :--- | :--- | :--- | :--- |
| `/api/auth/login` | `POST` | 用户登录 | `{ username, password }` |
| `/api/auth/register` | `POST` | 用户注册 | `{ email, username, password, code }` |
| `/api/auth/guest` | `POST` | 游客一键登录 | 无 |
| `/api/auth/email/send-code` | `POST` | 发送邮箱验证码 | `{ email, type }` (type: register/reset) |
| `/api/auth/password/reset` | `POST` | 重置密码 | `{ email, newPassword, code }` |
| `/api/auth/profile` | `GET` | 获取当前用户信息 | 无 (需 Token) |
| `/api/auth/streak` | `GET` | 获取连续签到天数 | 无 (用于个人中心展示) |

---

## 2. 题库模块 (Question Bank)
**后端控制器**: `QuestionController.java`, `QuestionBankController.java` | **对应前端**: `QuestionBank.vue`

| 接口路径 | 方法 | 说明 | 参数 / Body |
| :--- | :--- | :--- | :--- |
| `/api/questions` | `GET` | 分页获取题目列表 | `page, size, category, difficulty, keyword` |
| `/api/questions/{id}` | `GET` | 获取题目详情 | `id` (路径参数) |
| `/api/questions/{id}/answer` | `GET` | 获取题目标准答案 | `id` (查看解析时触发) |
| `/api/questions/categories` | `GET` | 获取所有题目分类 | 无 (用于筛选条) |
| `/api/questions/random` | `GET` | 随机抽取一道题 | `category, difficulty` (每日挑战) |
| `/api/question-banks` | `GET` | 获取题库集合 | 无 (预留模块) |

---

## 3. 协作手记模块 (Notes)
**后端控制器**: `NoteController.java` | **对应前端**: `Notes.vue`, `Me.vue`

| 接口路径 | 方法 | 说明 | 参数 / Body |
| :--- | :--- | :--- | :--- |
| `/api/notes` | `GET` | 分页获取公开手记 | `page, size, category, keyword` |
| `/api/notes/mine` | `GET` | 获取我的手记列表 | `status` (0:拒绝, 1:通过, 2:审核中) |
| `/api/notes/{id}` | `GET` | 获取手记详情内容 | `id` |
| `/api/notes` | `POST` | 发布新投影手记 | `{ title, content, summary, category, emoji }` |
| `/api/notes/{id}/like` | `POST` | 点赞/取消点赞 | 无 (自动识别身份) |
| `/api/notes/{id}/comments` | `GET` | 获取手记评论列表 | `id` |
| `/api/notes/{id}/comments` | `POST` | 发表评论 | `{ content }` |

---

## 4. 灵感愿望墙 (Wish Wall)
**后端控制器**: `WishWallController.java` | **对应前端**: `WishWall.vue`

| 接口路径 | 方法 | 说明 | 参数 / Body |
| :--- | :--- | :--- | :--- |
| `/api/wishes` | `GET` | 获取已审核愿望列表 | `visitorId` (非登录用户标识) |
| `/api/wishes` | `POST` | 投递新愿望 | `{ content, city }` |
| `/api/wishes/{id}/like` | `POST` | 为愿望能量充值 (点赞) | `{ visitorId }` |
| `/api/wishes/{id}/comments` | `GET` | 获取愿望留言 | `id` |

---

## 5. 排行榜与统计 (Leaderboard & Analytics)
**后端控制器**: `LeaderboardController.java`, `AnalyticsController.java`

| 接口路径 | 方法 | 说明 | 数据含义 |
| :--- | :--- | :--- | :--- |
| `/api/leaderboard/global` | `GET` | 全局活跃排行榜 | 返回前 N 名用户的积分与排名 |
| `/api/analytics/overview` | `GET` | 平台运营概览 | 题目总数、手记活跃度、愿望总数 |

---

## 6. AI 助手 (AI Assistant)
**后端控制器**: `AiController.java` | **对应前端**: `QuestionBank.vue` (AI 解析弹窗)

| 接口路径 | 方法 | 说明 | 参数 / Body |
| :--- | :--- | :--- | :--- |
| `/api/ai/question/analyze` | `POST` | AI 题目解析生成 | `{ questionId }` (流式或一次性返回) |
| `/api/ai/chat/direct` | `POST` | 直接与 AI 助手对话 | `{ message }` |

---

## 7. 后台管理模块 (Admin Only)
**后端前缀**: `/api/admin/...` | **对应前端**: `nova-admin` 项目

| 接口路径 | 方法 | 权限 | 说明 |
| :--- | :--- | :--- | :--- |
| `/api/admin/notes/list` | `GET` | `ADMIN` | 审核手记列表 |
| `/api/admin/notes/{id}/audit` | `POST` | `ADMIN` | 手记审核 (通过/拒绝) |
| `/api/admin/questions` | `POST` | `ADMIN` | 录入/导入新题目 |
| `/api/admin/wishes/{id}/status`| `PUT` | `ADMIN` | 愿望可见性批量控制 |

---

## 🛡 安全与限流

1.  **IP 限流**: 针对 `/api/auth/email/send-code` 接口，系统实施了 60s 的 IP + Email 双重冷却机制。
2.  **JWT 过期**: Token 默认有效期为 7 天。前端 `axios` 拦截器在收到 `401` 状态码时，应当清除 `localStorage` 并跳转至 `/login`。
3.  **CORS**: 后端已开启跨域支持，允许前端通过特定域名（或通配符）进行访问。
