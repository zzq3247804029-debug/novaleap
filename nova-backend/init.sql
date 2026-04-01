SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS novaleap DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE novaleap;


-- ==========================================
-- 1. 用户表 users
-- ==========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名/账号',
    password VARCHAR(255) NOT NULL COMMENT '密码 (BCrypt)',
    nickname VARCHAR(64) NOT NULL COMMENT '昵称',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN, USER, GUEST',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 预置超级管理员数据 (密码为: 123456 的 BCrypt 哈希值)
INSERT INTO users (username, password, nickname, role) VALUES 
('admin', '$2a$10$7XyH4O4xQ0M.0K7lH0O8I.d0K7lH0O8I.d0K7lH0O8I.d0K7lH0O8', '超级管理员', 'ADMIN') 
ON DUPLICATE KEY UPDATE id=id;

-- ==========================================
-- 2. 星愿墙数据表 wishes
-- ==========================================
CREATE TABLE IF NOT EXISTS wishes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL COMMENT '愿望内容',
    emotion VARCHAR(32) COMMENT '情绪分析结果 (happy, hopeful, confused, anxious)',
    color VARCHAR(32) COMMENT 'UI 面板颜色',
    city VARCHAR(64) COMMENT '城市/坐标',
    pos_x INT COMMENT 'X坐标百分比 0-100',
    pos_y INT COMMENT 'Y坐标百分比 0-100',
    float_speed DOUBLE COMMENT '漂浮速度调节因子',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0: 待审核 1: 已上线 2: 违规',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 1是, 0否'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='星愿墙数据表';

-- ==========================================
-- 3. 题库表 questions
-- ==========================================
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL COMMENT '题目标题',
    content TEXT COMMENT '题目详细描述',
    standard_answer MEDIUMTEXT COMMENT '数据库标准答案（供AI回答）',
    difficulty TINYINT NOT NULL DEFAULT 2 COMMENT '难度: 1简单 2中等 3困难',
    category VARCHAR(64) NOT NULL DEFAULT 'java' COMMENT '分类: java, spring, db, redis, algo, network',
    tags VARCHAR(255) COMMENT '标签 (逗号分隔)',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0: 下架 1: 上架',
    source_type VARCHAR(16) NOT NULL DEFAULT 'OFFICIAL' COMMENT '来源类型: OFFICIAL, CUSTOM',
    custom_bank_id BIGINT NULL COMMENT '自定义题库ID',
    owner_user_id BIGINT NULL COMMENT '题库所属用户ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

CREATE TABLE IF NOT EXISTS custom_question_banks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '上传用户 ID',
    name VARCHAR(255) NOT NULL COMMENT '题库名',
    original_file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_type VARCHAR(16) NOT NULL COMMENT 'txt',
    raw_content MEDIUMTEXT NOT NULL COMMENT '解析后的原始文本',
    category VARCHAR(64) NOT NULL DEFAULT 'java' COMMENT '默认分类',
    difficulty TINYINT NOT NULL DEFAULT 2 COMMENT '默认难度',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0: 待审核 1: 已通过 2: 已驳回',
    question_count INT NOT NULL DEFAULT 0 COMMENT '解析题目数',
    imported_question_count INT NOT NULL DEFAULT 0 COMMENT '正式入库题目数',
    reject_reason VARCHAR(240) NULL COMMENT '驳回原因',
    audited_at DATETIME NULL COMMENT '审核时间',
    imported_at DATETIME NULL COMMENT '正式入库时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_custom_question_banks_user_status (user_id, status, created_at),
    KEY idx_custom_question_banks_status_created (status, created_at),
    CONSTRAINT fk_custom_question_banks_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户自定义题库审核表';

-- ==========================================
-- 4. 灵感手记表 notes
-- ==========================================
CREATE TABLE IF NOT EXISTS notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL COMMENT '手记标题',
    content MEDIUMTEXT NOT NULL COMMENT '手记正文',
    summary VARCHAR(500) NULL COMMENT '手记摘要',
    category VARCHAR(64) NOT NULL DEFAULT '技术手记' COMMENT '手记分类',
    emoji VARCHAR(16) DEFAULT '📝' COMMENT '封面表情',
    author VARCHAR(64) NOT NULL DEFAULT 'Nova 学员' COMMENT '作者名',
    user_id BIGINT NULL COMMENT '作者用户ID，关联 users.id',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0: 待审核 1: 审核通过 2: 审核失败',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 1是, 0否',
    KEY idx_notes_status_created (status, created_at),
    KEY idx_notes_category (category),
    KEY idx_notes_user_id (user_id),
    CONSTRAINT fk_notes_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灵感手记表';

-- 5. 手记互动表（点赞/评论）
CREATE TABLE IF NOT EXISTS note_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id BIGINT NOT NULL,
    actor_type VARCHAR(16) NOT NULL COMMENT 'user / guest',
    actor_id VARCHAR(128) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_note_like_actor (note_id, actor_type, actor_id),
    KEY idx_note_likes_note_id (note_id),
    KEY idx_note_likes_actor (actor_type, actor_id),
    CONSTRAINT fk_note_likes_note FOREIGN KEY (note_id) REFERENCES notes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手记点赞表';

CREATE TABLE IF NOT EXISTS note_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    content VARCHAR(300) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_note_comments_note_id_created (note_id, created_at),
    KEY idx_note_comments_user_id (user_id),
    CONSTRAINT fk_note_comments_note FOREIGN KEY (note_id) REFERENCES notes(id),
    CONSTRAINT fk_note_comments_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手记评论表';

-- 预置种子题目数据
INSERT INTO questions (title, content, standard_answer, difficulty, category, tags, view_count) VALUES
('Redis 缓存雪崩、穿透、击穿如何解决？', '请详细描述这三种问题的不同场景，并给出对应的解决方案和预防措施。特别强调在分布式锁和布隆过滤器中的应用细节。', '【标准答案】\n1) 缓存穿透：请求查询不存在的数据，打到 DB。方案：布隆过滤器 + 空值缓存 + 接口限流。\n2) 缓存击穿：热点 Key 失效瞬间并发打 DB。方案：互斥锁重建缓存、逻辑过期、热点预热。\n3) 缓存雪崩：大量 Key 同时失效。方案：过期时间随机化、分批预热、多级缓存、降级熔断。', 2, 'redis', 'Redis,缓存机制,高可用', 342),
('Spring Bean 的生命周期全解析', '面试高频题：请从 Bean 的实例化、属性填充、初始化前/后处理、销毁等阶段完整描述，并说明 BeanPostProcessor 的作用。', '【标准答案】\n核心流程：实例化 -> 属性注入 -> Aware 回调 -> BeanPostProcessor#postProcessBeforeInitialization -> InitializingBean / init-method -> BeanPostProcessor#postProcessAfterInitialization -> 使用 -> DisposableBean / destroy-method。\nBeanPostProcessor 是扩展点，AOP 代理通常在后置处理中创建。', 3, 'spring', 'Spring Core,源码剖析', 512),
('TCP 三次握手与四次挥手', '解释为什么握手是三次而挥手需要四次？如果服务端收到 SYN 后立即进入 ESTABLISHED 状态会有什么风险？', '【标准答案】\n三次握手用于双方确认收发能力与初始序列号：SYN -> SYN+ACK -> ACK。\n若服务端收到 SYN 立即 ESTABLISHED，会遭受半连接与伪造 SYN 风险。\n四次挥手因为 TCP 全双工，双方关闭发送通道各需一次 FIN 与 ACK。', 2, 'network', '计算机网络,TCP/IP', 189),
('MySQL 索引失效场景总结', '请列举 5 种以上会导致 MySQL 索引失效的场景，并说明底层的 B+ 树为什么会失效。', '【标准答案】\n常见失效：\n1) 对索引列使用函数/表达式；2) 隐式类型转换；3) LIKE 前置通配符；4) 联合索引未满足最左前缀；5) OR 条件一侧无索引；6) 选择性太差导致优化器放弃。\n本质是无法按索引有序性快速定位范围，回退全表扫描。', 2, 'db', 'MySQL,SQL优化', 226),
('如何实现一个线程安全的单例模式？', '请手写 DCL（双重检查锁）单例模式，并解释 volatile 关键字在此处的具体作用（指令重排）。', '【标准答案】\n使用 DCL：先判空，再进入 synchronized，再判空后 new。\ninstance 必须是 volatile，防止 new 的指令重排（分配内存->赋引用->初始化）导致读取到未初始化对象。', 1, 'java', '设计模式,多线程', 433)
ON DUPLICATE KEY UPDATE id=id;

-- 预置手记数据
INSERT INTO notes (title, content, category, emoji, author, view_count, status) VALUES
('深入浅出 Vue 3 渲染器设计与实现',
'## 为什么需要自定义渲染器？
Vue 3 的一大亮点是将渲染核心与具体宿主环境解耦。这意味着不仅可以在浏览器（DOM）中渲染，还能在 Canvas 甚至终端中渲染界面。

### 核心方法拆解
```javascript
const { createRenderer } = Vue
const renderer = createRenderer({
  createElement(tag) {},
  setElementText(node, text) {},
  insert(el, parent, anchor = null) {}
})
```
通过拦截这些原生操作，我们赋予了框架更强的扩展能力。',
'前端工程化', '🟢', '志琪的笔记', 1240, 1),

('Kafka 分区与偏移量设计精解',
'这是关于 Kafka 的学习笔记。

### 消费者组 Rebalance
当有新的 Consumer 加入或旧的 Consumer 崩溃退出时，会触发 Rebalance 机制来重新分配 Partition。',
'分布式系统', '📝', '志琪的笔记', 89, 1)
ON DUPLICATE KEY UPDATE id=id;

-- 预置种子星愿数据
-- 1. 修复可能存在的编码问题，确保末尾有分号
INSERT INTO wishes 
(content, emotion, color, city, pos_x, pos_y, float_speed, status)
VALUES
('希望能拿到字节跳动的 Offer，加油冲呀！🚀', 'hopeful', '#D4E0D0', '河北', 15, 20, 1.2, 1),
('Q3结束前彻底掌握 React Hooks！💻', 'determined', '#DDBFD1', '上海', 55, 35, 1.0, 1),
('今年想为一个大型开源项目贡献代码。一起飞跃吧！✨', 'hopeful', '#C4D0DE', '北京', 25, 60, 0.9, 1),
('搞定系统设计面试，不再畏惧架构图！🏗️', 'confident', '#D7E0CA', '成都', 65, 70, 1.5, 1),
('正从 UI 转型全栈开发。一步一个脚印。🎨', 'determined', '#E0D2C3', '深圳', 75, 25, 0.8, 1);
