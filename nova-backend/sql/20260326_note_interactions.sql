SET NAMES utf8mb4;
USE novaleap;

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

