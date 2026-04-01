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

ALTER TABLE questions
    ADD COLUMN source_type VARCHAR(16) NOT NULL DEFAULT 'OFFICIAL' COMMENT '来源类型：OFFICIAL/CUSTOM';

ALTER TABLE questions
    ADD COLUMN custom_bank_id BIGINT NULL COMMENT '自定义题库 ID';

ALTER TABLE questions
    ADD COLUMN owner_user_id BIGINT NULL COMMENT '题目所属用户 ID';

CREATE INDEX idx_questions_source_created ON questions (source_type, created_at);
CREATE INDEX idx_questions_custom_bank ON questions (custom_bank_id);
CREATE INDEX idx_questions_owner_source ON questions (owner_user_id, source_type);

UPDATE questions
SET source_type = 'OFFICIAL'
WHERE source_type IS NULL OR source_type = '';
