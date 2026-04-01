SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS novaleap DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE novaleap;

SET @schema_name = DATABASE();

-- 1) 增加标准答案字段（幂等）
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE questions ADD COLUMN standard_answer MEDIUMTEXT COMMENT ''数据库标准答案（供AI回答）'' AFTER content',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = @schema_name AND table_name = 'questions' AND column_name = 'standard_answer'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) 为空答案回填，避免 AI 回答无内容
UPDATE questions
SET standard_answer = CONCAT(
    '【数据库标准答案】\n',
    IFNULL(content, '请在管理后台补充此题标准答案。')
)
WHERE standard_answer IS NULL OR TRIM(standard_answer) = '';

-- 3) 额外提供一条可用于联调的题目（如不存在）
INSERT INTO questions (title, content, standard_answer, difficulty, category, tags, view_count, status)
SELECT
    '系统设计：如何设计一个高可用秒杀系统？',
    '请从流量削峰、库存一致性、幂等处理、降级熔断和监控报警几个角度回答。',
    '【标准答案】\n1) 入口限流与验证码防刷；2) Redis 预减库存 + 异步下单削峰；3) 订单幂等键防重；4) MQ + 本地事务保证最终一致；5) 热点隔离与熔断降级；6) 全链路监控与告警。',
    3,
    'db',
    '系统设计,高并发,削峰填谷',
    0,
    1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM questions WHERE title = '系统设计：如何设计一个高可用秒杀系统？'
);
