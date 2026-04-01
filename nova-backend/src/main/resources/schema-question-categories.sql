CREATE TABLE IF NOT EXISTS question_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE COMMENT 'category code',
    name VARCHAR(64) NOT NULL COMMENT 'category name',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'display order',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '1=enabled, 0=disabled',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO question_categories (code, name, sort_order, enabled) VALUES
('java', 'Java 核心', 1, 1),
('spring', 'Spring 生态', 2, 1),
('db', '数据存储', 3, 1),
('redis', 'Redis', 4, 1),
('algo', '算法', 5, 1),
('network', '计算机网络', 6, 1),
('system-design', '系统设计', 7, 1),
('linux', 'Linux', 8, 1),
('arch', '架构设计', 9, 1);

-- Normalize category codes used by questions/custom banks (兼容历史编码).
UPDATE questions SET category = 'db' WHERE category IN ('database', '数据库');
UPDATE custom_question_banks SET category = 'db' WHERE category IN ('database', '数据库');

UPDATE questions SET category = 'algo' WHERE category IN ('algorithm');
UPDATE custom_question_banks SET category = 'algo' WHERE category IN ('algorithm');

UPDATE questions SET category = 'network' WHERE category IN ('计算机网络');
UPDATE custom_question_banks SET category = 'network' WHERE category IN ('计算机网络');

UPDATE questions SET category = 'system-design' WHERE category IN ('system-sign', 'system_sign', 'system design', '系统设计');
UPDATE custom_question_banks SET category = 'system-design' WHERE category IN ('system-sign', 'system_sign', 'system design', '系统设计');

UPDATE questions SET category = 'arch' WHERE category IN ('architecture', '架构', '架构设计');
UPDATE custom_question_banks SET category = 'arch' WHERE category IN ('architecture', '架构', '架构设计');

-- Remove legacy alias rows after canonical rows are ensured.
DELETE FROM question_categories WHERE code IN ('database', 'algorithm', '计算机网络', 'system-sign', 'system_sign', 'system design');

-- Keep canonical labels/order.
UPDATE question_categories SET name = 'Java 核心', sort_order = 1, enabled = 1 WHERE code = 'java';
UPDATE question_categories SET name = 'Spring 生态', sort_order = 2, enabled = 1 WHERE code = 'spring';
UPDATE question_categories SET name = '数据存储', sort_order = 3, enabled = 1 WHERE code = 'db';
UPDATE question_categories SET name = 'Redis', sort_order = 4, enabled = 1 WHERE code = 'redis';
UPDATE question_categories SET name = '算法', sort_order = 5, enabled = 1 WHERE code = 'algo';
UPDATE question_categories SET name = '计算机网络', sort_order = 6, enabled = 1 WHERE code = 'network';
UPDATE question_categories SET name = '系统设计', sort_order = 7, enabled = 1 WHERE code = 'system-design';
UPDATE question_categories SET name = 'Linux', sort_order = 8, enabled = 1 WHERE code = 'linux';
UPDATE question_categories SET name = '架构设计', sort_order = 9, enabled = 1 WHERE code = 'arch';
