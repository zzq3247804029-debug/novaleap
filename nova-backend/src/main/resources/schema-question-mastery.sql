CREATE TABLE IF NOT EXISTS user_question_mastery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '做题用户ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    confirmed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '用户确认会做的时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_question_mastery_user_question (user_id, question_id),
    KEY idx_user_question_mastery_user (user_id),
    KEY idx_user_question_mastery_question (question_id),
    CONSTRAINT fk_user_question_mastery_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_user_question_mastery_question FOREIGN KEY (question_id) REFERENCES questions(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户题目掌握确认记录';
