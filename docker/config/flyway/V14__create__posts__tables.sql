-- 게시글 도메인 테이블 생성
USE `mysql_db`;

-- 게시글 테이블
CREATE TABLE IF NOT EXISTS `posts` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at`   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at`  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    `member_id`    BIGINT       NOT NULL,
    `title`        VARCHAR(255) NOT NULL,
    `content`      TEXT         NOT NULL,
    `view_count`   INT          NOT NULL DEFAULT 0,

    PRIMARY KEY (`id`),

    KEY `idx_posts_member` (`member_id`),
    KEY `idx_posts_created` (`created_at`),

    CONSTRAINT `fk_posts_member`
    FOREIGN KEY (`member_id`)
    REFERENCES `members` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 게시글 이미지 테이블
CREATE TABLE IF NOT EXISTS `post_images` (
    `id`        BIGINT       NOT NULL AUTO_INCREMENT,
    `post_id`   BIGINT       NOT NULL,
    `image_url` VARCHAR(500) NOT NULL,

    PRIMARY KEY (`id`),

    KEY `idx_post_images_post` (`post_id`),

    CONSTRAINT `fk_post_images_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `posts` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 게시글 좋아요 테이블
CREATE TABLE IF NOT EXISTS `post_likes` (
    `member_id`  BIGINT      NOT NULL,
    `post_id`    BIGINT      NOT NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`member_id`, `post_id`),

    KEY `idx_post_likes_post` (`post_id`),

    CONSTRAINT `fk_post_likes_member`
    FOREIGN KEY (`member_id`)
    REFERENCES `members` (`id`)
    ON DELETE CASCADE,

    CONSTRAINT `fk_post_likes_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `posts` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

