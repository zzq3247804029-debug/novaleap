SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS novaleap DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE novaleap;

-- 手记主表（内容/摘要）
CREATE TABLE IF NOT EXISTS notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL COMMENT '手记标题',
    content MEDIUMTEXT NOT NULL COMMENT '手记正文',
    summary VARCHAR(500) NULL COMMENT '手记摘要',
    category VARCHAR(64) NOT NULL DEFAULT '技术手记' COMMENT '手记分类',
    emoji VARCHAR(16) DEFAULT '📘' COMMENT '封面表情',
    author VARCHAR(64) NOT NULL DEFAULT 'Nova 学员' COMMENT '作者名',
    user_id BIGINT NULL COMMENT '作者用户ID，关联 users.id',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0: 待审核 1: 审核通过 2: 审核失败',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 1是 0否'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灵感手记表';

SET @schema_name = DATABASE();

-- 兼容旧库：缺列则补
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE notes ADD COLUMN summary VARCHAR(500) NULL COMMENT ''手记摘要''',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'notes' AND column_name = 'summary'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE notes ADD COLUMN user_id BIGINT NULL COMMENT ''作者用户ID，关联 users.id''',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'notes' AND column_name = 'user_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE notes ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'notes' AND column_name = 'updated_at'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE notes ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT ''逻辑删除标记: 1是 0否''',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'notes' AND column_name = 'deleted'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 兼容旧库：缺索引则补
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE notes ADD INDEX idx_notes_status_created (status, created_at)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = @schema_name AND table_name = 'notes' AND index_name = 'idx_notes_status_created'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE notes ADD INDEX idx_notes_category (category)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = @schema_name AND table_name = 'notes' AND index_name = 'idx_notes_category'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE notes ADD INDEX idx_notes_user_id (user_id)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = @schema_name AND table_name = 'notes' AND index_name = 'idx_notes_user_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 外键仅在 users 表存在且尚未创建时补充
SET @users_exists = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = @schema_name AND table_name = 'users'
);
SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE constraint_schema = @schema_name
      AND table_name = 'notes'
      AND constraint_name = 'fk_notes_user'
);
SET @sql = IF(
    @users_exists > 0 AND @fk_exists = 0,
    'ALTER TABLE notes ADD CONSTRAINT fk_notes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 手记互动表（点赞/评论）
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Notes likes';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Notes comments';

INSERT INTO notes (title, content, summary, category, emoji, author, view_count, status)
SELECT
    '深入浅出 Vue 3 渲染器设计与实现',
    '## 为什么需要自定义渲染器？\nVue 3 的渲染核心与宿主环境解耦，使得同一套响应式机制可以在 DOM、Canvas 等场景复用。\n\n### 核心思路\n通过 `createRenderer` 注入宿主操作：createElement / insert / setElementText 等，实现跨平台渲染。',
    'Vue 3 渲染器的设计目标与 createRenderer 关键机制拆解。',
    '前端工程化',
    '🧩',
    '志琪的笔记',
    1240,
    1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM notes WHERE title = '深入浅出 Vue 3 渲染器设计与实现'
);

INSERT INTO notes (title, content, summary, category, emoji, author, view_count, status)
SELECT
    'Kafka 分区与偏移量设计精解',
    'Kafka 通过 Partition + Offset 管理消息顺序与消费进度。消费者组 Rebalance 会在实例变更时重新分配分区，需要关注幂等消费与位点提交策略。',
    '分区、位点、Rebalance 的实战要点与常见坑位。',
    '分布式系统',
    '📘',
    '志琪的笔记',
    89,
    1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM notes WHERE title = 'Kafka 分区与偏移量设计精解'
);
