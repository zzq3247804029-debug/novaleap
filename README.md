# NovaLeap - 全栈智能化学习与协作平台

NovaLeap 是一个现代化的、功能丰富的全栈 Web 应用程序，旨在为学生和开发者提供一个集学习、笔记、愿望投递与 AI 辅助于一体的综合性平台。项目采用先进的微服务架构思想（逻辑分层），结合了高性能的后端技术栈与极致视觉体验的前端设计。

---

## 🌟 核心功能

-   **📝 协作手记 (Notes)**：支持 Markdown 渲染的一体化手记系统。包含分类管理、摘要自动生成、点赞社交交互及评论系统。
-   **📚 智能题库 (Question Bank)**：涵盖多种编程语言与技术栈的面试题库。支持难度分级、分类检索，并集成 AI 加速解析。
-   **🌌 灵感愿望墙 (Wish Wall)**：独特的 3D 浮动感愿望系统。用户可以投递彩色愿望泡泡，支持情绪感应颜色分配与自动审核流。
-   **🤖 AI 深度集成**：内置 AI 助手，可针对题库进行解析，通过 LLM（如 GPT-4 兼容模型）提供智能问答支持。
-   **🏆 实时排行榜 (Leaderboard)**：基于 Redis 实现的高性能积分排行榜，追踪用户在社区中的活跃度与贡献。
-   **🛠 后台管理系统 (Admin)**：完善的资源审计流程，支持对手记、题目、愿望及用户反馈进行全方位监控与审核。
-   **🛡 安全防护**：基于两层架构（IP + 邮箱）的验证码频率限制，防止恶意刷取接口。

---

## 🛠 技术栈

### 前端 (nova-frontend)
-   **框架**: Vue 3 (Composition API)
-   **构建工具**: Vite
-   **样式**: CSS Variables + Tailwind CSS (用于现代布局)
-   **图标**: Lucide Vue Next
-   **状态管理**: Pinia / Reactive Store
-   **网络请求**: Axios

### 后端 (nova-backend)
-   **核心**: Java 17 + Spring Boot 3.2.5
-   **安全**: Spring Security + JWT (Stateless)
-   **持久层**: MyBatis Plus 3.5.5
-   **数据库**: MySQL 8.0
-   **缓存/并发**: Redis 7.0
-   **AI 框架**: Spring AI
-   **邮件服务**: Resend API

---

## 🚀 快速开始

### 前项要求
-   JDK 17+
-   Node.js 18+
-   MySQL 8.0+
-   Redis 7.0+

### 1. 数据库准备
新建数据库 `novaleap`，并执行根目录下的 `nova-backend/init.sql` 脚本进行表结构初始化。

### 2. 后端配置 (nova-backend)
修改 `src/main/resources/application-dev.yml` 中的数据库连接信息、Redis 配置及 API 密钥。
```bash
mvn clean install
mvn spring-boot:run
```

### 3. 前端启动 (nova-frontend)
```bash
npm install
npm run dev
```

### 4. Docker 一键部署
项目支持 Docker Compose 容器化部署：
```bash
docker-compose up -d --build
```
默认暴露端口：
-   Frontend: `http://localhost:80`
-   Admin: `http://localhost:8081`
-   Backend API: `http://localhost:8080`

---

## 📂 项目结构

```text
novalap/
├── nova-backend/      # Spring Boot 后端源码
├── nova-frontend/     # 用户端 Vue 3 源码
├── nova-admin/        # (可选) 独立管理端前端
├── docker-compose.yml # 容器化编排配置
└── init.sql           # 数据库初始脚本
```

---

## 🤝 贡献与反馈

如果您在使用过程中发现任何 Bug 或有新的功能提议，欢迎通过项目的“意见反馈”模块或直接在 GitHub 提交 Issue。

---

## 📄 开源协议
[MIT License](LICENSE)
