-- 댓글 도메인 테이블 생성
USE `mysql_db`;

-- 댓글 테이블
CREATE TABLE IF NOT EXISTS `comments` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at`  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at` DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    `member_id`   BIGINT       NOT NULL,
    `post_id`     BIGINT       NOT NULL,
    `parent_id`   BIGINT       NULL,
    `content`     TEXT         NOT NULL,

    PRIMARY KEY (`id`),

    KEY `idx_comment_post` (`post_id`),
    KEY `idx_comment_parent` (`parent_id`),
    KEY `idx_comment_member` (`member_id`),

    CONSTRAINT `fk_comments_member`
    FOREIGN KEY (`member_id`)
    REFERENCES `members` (`id`)
    ON DELETE CASCADE,

    CONSTRAINT `fk_comments_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `posts` (`id`)
    ON DELETE CASCADE,

    CONSTRAINT `fk_comments_parent`
    FOREIGN KEY (`parent_id`)
    REFERENCES `comments` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 댓글 좋아요 테이블
CREATE TABLE IF NOT EXISTS `comment_likes` (
    `member_id`  BIGINT      NOT NULL,
    `comment_id` BIGINT      NOT NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`member_id`, `comment_id`),

    KEY `idx_comment_likes_comment` (`comment_id`),

    CONSTRAINT `fk_comment_likes_member`
    FOREIGN KEY (`member_id`)
    REFERENCES `members` (`id`)
    ON DELETE CASCADE,

    CONSTRAINT `fk_comment_likes_comment`
    FOREIGN KEY (`comment_id`)
    REFERENCES `comments` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

