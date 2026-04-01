SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS novaleap DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE novaleap;

CREATE TABLE IF NOT EXISTS wish_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wish_id BIGINT NOT NULL,
    actor_type VARCHAR(16) NOT NULL COMMENT 'user/guest/visitor',
    actor_id VARCHAR(128) NOT NULL COMMENT 'username or visitor id',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wish_like_actor (wish_id, actor_type, actor_id),
    KEY idx_wish_likes_wish_id (wish_id),
    KEY idx_wish_likes_actor (actor_type, actor_id),
    CONSTRAINT fk_wish_likes_wish FOREIGN KEY (wish_id) REFERENCES wishes(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Wish wall likes';

CREATE TABLE IF NOT EXISTS wish_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wish_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    content VARCHAR(300) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'logical delete',
    KEY idx_wish_comments_wish_id_created (wish_id, created_at),
    KEY idx_wish_comments_user_id (user_id),
    CONSTRAINT fk_wish_comments_wish FOREIGN KEY (wish_id) REFERENCES wishes(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_wish_comments_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Wish wall comments';
