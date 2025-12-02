SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';

CREATE SCHEMA IF NOT EXISTS `mysql_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `mysql_db` ;

--  멤버 테이블
CREATE TABLE IF NOT EXISTS `members` (
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT,
    `created_at`         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at`        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                        ON UPDATE CURRENT_TIMESTAMP(6),

    -- 기본 정보
    `email`              VARCHAR(255)  NOT NULL UNIQUE,
    `password`           VARCHAR(255)  NOT NULL,
    `name`               VARCHAR(50)   NOT NULL,
    `nickname`           VARCHAR(50)   NOT NULL,

    -- 프로필 정보
    `country`            VARCHAR(50)   NOT NULL,
    `english_level`      VARCHAR(20)   NOT NULL,
    `interests`          TEXT          NOT NULL,
    `description`        TEXT          NULL,
    `profile_image_url`  VARCHAR(255),

    -- 권한 및 멤버십
    `role`               VARCHAR(20)   NOT NULL,
    `membership_grade`   VARCHAR(20)   NOT NULL,

    -- 로그인 / 차단 / 삭제 상태
    `last_seen_at`       DATETIME(6),  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `is_blocked`         BOOLEAN       NOT NULL DEFAULT FALSE,
    `blocked_at`         DATETIME(6),
    `block_reason`       VARCHAR(255),
    `is_deleted`         BOOLEAN       NOT NULL DEFAULT FALSE,
    `deleted_at`         DATETIME(6),

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `friendships` (
                                             `id`                 BIGINT        NOT NULL AUTO_INCREMENT,
                                             `smaller_member_id`  BIGINT        NOT NULL,
                                             `larger_member_id`   BIGINT        NOT NULL,
                                             `created_at`         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_smaller_larger_member` (`smaller_member_id`, `larger_member_id`),

    CONSTRAINT `fk_friendships_smaller_member`
    FOREIGN KEY (`smaller_member_id`) REFERENCES `members` (`id`),

    CONSTRAINT `fk_friendships_larger_member`
    FOREIGN KEY (`larger_member_id`)  REFERENCES `members` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `friendship_requests` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `sender_id`    BIGINT        NOT NULL,
    `receiver_id`  BIGINT        NOT NULL,
    `created_at`   DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_sender_receiver_pair` (`sender_id`, `receiver_id`),

    CONSTRAINT `fk_friendship_requests_sender`
    FOREIGN KEY (`sender_id`)   REFERENCES `members` (`id`),

    CONSTRAINT `fk_friendship_requests_receiver`
    FOREIGN KEY (`receiver_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- AI 테이블
CREATE TABLE IF NOT EXISTS `system_prompts` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `created_at`   DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    `prompt_key`   VARCHAR(100)  NOT NULL UNIQUE,
    `description`  VARCHAR(255)  NOT NULL,
    `content`      TEXT          NOT NULL,
    `version`      INT           NOT NULL,

    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_prompts` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `created_at`    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at`   DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    `member_id`     BIGINT        NULL,

    `prompt_type`   VARCHAR(20)   NOT NULL,
    `title`         VARCHAR(255)  NOT NULL,
    `content`       TEXT          NOT NULL,
    `scenario_id`   VARCHAR(50)   NOT NULL UNIQUE,

    PRIMARY KEY (`id`),
    FOREIGN KEY (`member_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 학습노트 테이블
CREATE TABLE IF NOT EXISTS `learning_notes` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT,
    `member_id`         BIGINT        NOT NULL,
    `original_content`  VARCHAR(100)  NOT NULL,
    `corrected_content` VARCHAR(100)  NOT NULL,
    `created_at`        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`id`),
    KEY `idx_learning_notes_member_id` (`member_id`),

    CONSTRAINT `fk_learning_note_member`
    FOREIGN KEY (`member_id`) REFERENCES `members` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `feedbacks` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `learning_note_id` BIGINT       NOT NULL,
    `created_at`       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at`      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `tag`              VARCHAR(50)  NOT NULL,
    `problem`          VARCHAR(100) NOT NULL,
    `correction`       VARCHAR(100) NOT NULL,
    `extra`            VARCHAR(100) NOT NULL,
    `is_marked`        BOOLEAN      NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`id`),
    KEY `idx_learning_feedbacks_learning_note_id` (`learning_note_id`),

    CONSTRAINT `fk_feedback_note`
    FOREIGN KEY (`learning_note_id`)
    REFERENCES `learning_notes` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `sentence_games` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT,
    `created_at`        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    `original_content`  TEXT          NOT NULL,
    `corrected_content` TEXT          NOT NULL,

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 신고 테이블
CREATE TABLE IF NOT EXISTS `reports` (
    `id`                   BIGINT        NOT NULL AUTO_INCREMENT,
    `created_at`           DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at`          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    `reported_msg_content` TEXT          NULL,
    `target_member_id`     BIGINT        NOT NULL,
    `status`               VARCHAR(10)   NOT NULL,
    `category`             VARCHAR(20)   NOT NULL,
    `reported_reason`      VARCHAR(255)  NULL,

    PRIMARY KEY (`id`),
    KEY `idx_reports_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 채팅 테이블
CREATE TABLE IF NOT EXISTS `ai_chat_rooms` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at`   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at`  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    `name`         VARCHAR(255) NOT NULL,
    `ai_model_id`  VARCHAR(255) NOT NULL,
    `ai_persona`   VARCHAR(255) NOT NULL,

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `chat_members` (
    `id`                        BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at`                DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at`               DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)  ON UPDATE CURRENT_TIMESTAMP(6),

    `member_id`                 BIGINT       NOT NULL,
    `chat_room_id`              BIGINT       NOT NULL,
    `chat_room_type`            VARCHAR(50)  NOT NULL,
    `last_read_at`              DATETIME(6),
    `last_read_sequence`        BIGINT,
    `chat_notification_setting` VARCHAR(50)  NOT NULL,

    PRIMARY KEY (`id`),

    KEY `idx_chat_members_member_id` (`member_id`),
    KEY `idx_chat_members_room` (`chat_room_id`, `chat_room_type`),

    CONSTRAINT `fk_chat_members_member`
    FOREIGN KEY (`member_id`)
    REFERENCES `members` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `direct_chat_rooms` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at`   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at`  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    `user1_id`     BIGINT       NOT NULL,
    `user2_id`     BIGINT       NOT NULL,
    `current_sequence` BIGINT   NOT NULL DEFAULT 0,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_user1_user2` (`user1_id`, `user2_id`),

    KEY `idx_direct_chat_user1` (`user1_id`),
    KEY `idx_direct_chat_user2` (`user2_id`),

    CONSTRAINT `fk_direct_chat_user1`
    FOREIGN KEY (`user1_id`)
    REFERENCES `members` (`id`),

    CONSTRAINT `fk_direct_chat_user2`
    FOREIGN KEY (`user2_id`)
    REFERENCES `members` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `group_chat_rooms` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at`   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at`  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    `name`         VARCHAR(255) NOT NULL,
    `description`  VARCHAR(500),
    `topic`        VARCHAR(50),
    `owner_id`     BIGINT       NOT NULL,
    `password`     VARCHAR(255),
    `current_sequence` BIGINT   NOT NULL DEFAULT 0,

    PRIMARY KEY (`id`),

    KEY `idx_group_chat_owner` (`owner_id`),

    CONSTRAINT `fk_group_chat_owner`
    FOREIGN KEY (`owner_id`)
    REFERENCES `members` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

CREATE TABLE IF NOT EXISTS `notifications` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `created_at`   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at`  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    `receiver_id`  BIGINT       NOT NULL,
    `sender_id`    BIGINT,
    `type`         VARCHAR(50)  NOT NULL,
    `content`      VARCHAR(255),
    `is_read`      BOOLEAN      NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`id`),

    KEY `idx_notifications_receiver` (`receiver_id`),
    KEY `idx_notifications_sender`   (`sender_id`),
    KEY `idx_notifications_type`     (`type`),

    CONSTRAINT `fk_notifications_receiver`
    FOREIGN KEY (`receiver_id`)
    REFERENCES `members` (`id`)
    ON DELETE CASCADE,

    CONSTRAINT `fk_notifications_sender`
    FOREIGN KEY (`sender_id`)
    REFERENCES `members` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
