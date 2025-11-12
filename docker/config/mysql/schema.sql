SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';

-- CREATE SCHEMA IF NOT EXISTS `mysql_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
-- CREATE SCHEMA IF NOT EXISTS `mysql_test` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
-- USE `mysql_db` ;

CREATE TABLE IF NOT EXISTS `members` (
                                         `id`               BIGINT       NOT NULL AUTO_INCREMENT,
                                         `created_at`       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                         `modified_at`      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                                         `email`            VARCHAR(255) NOT NULL UNIQUE,
                                         `password`         VARCHAR(255) NOT NULL,
                                         `name`             VARCHAR(50)  NOT NULL,
                                         `nickname`         VARCHAR(50)  NOT NULL,

                                         `country`          VARCHAR(50)  NOT NULL,
                                         `english_level`    VARCHAR(20)  NOT NULL,
                                         `interest`         VARCHAR(100) NOT NULL,
                                         `description`      TEXT NULL,

                                         `role`             VARCHAR(20)  NOT NULL,
                                         `membership_grade` VARCHAR(20)  NOT NULL,

                                         `last_sign_in_at`  DATETIME(6),

                                         `is_blocked`       BOOLEAN      NOT NULL DEFAULT FALSE,
                                         `blocked_at`       DATETIME(6),
                                         `block_reason`     VARCHAR(255),

                                         `is_deleted`       BOOLEAN      NOT NULL DEFAULT FALSE,
                                         `deleted_at`       DATETIME(6),

                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `friendships` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `smaller_member_id` BIGINT       NOT NULL,
    `larger_member_id`  BIGINT       NOT NULL,

    `created_at`        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),


    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_smaller_larger_member` (`smaller_member_id`, `larger_member_id`),

    -- 외래 키 제약 조건
    CONSTRAINT `fk_friendships_smaller_member`
    FOREIGN KEY (`smaller_member_id`)
    REFERENCES `members` (`id`),

    CONSTRAINT `fk_friendships_larger_member`
    FOREIGN KEY (`larger_member_id`)
    REFERENCES `members` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `friendship_requests` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `sender_id`        BIGINT       NOT NULL,
    `receiver_id`      BIGINT       NOT NULL,

    `created_at`       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sender_receiver_pair` (`sender_id`, `receiver_id`),

    -- 외래 키 제약 조건
    CONSTRAINT `fk_friendship_requests_sender`
    FOREIGN KEY (`sender_id`)
    REFERENCES `members` (`id`),

    CONSTRAINT `fk_friendship_requests_receiver`
    FOREIGN KEY (`receiver_id`)
    REFERENCES `members` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS `reports` (
                                         `id`              BIGINT       NOT NULL AUTO_INCREMENT,
                                         `created_at`      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                         `modified_at`     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                         `reported_msg_content`  TEXT         NULL,
                                         `target_member_id` BIGINT      NOT NULL,
                                         `status`          VARCHAR(10)  NOT NULL,
                                         `category`          VARCHAR(20) NOT NULL,
                                         `reported_reason`     VARCHAR(255) NULL,

                                         PRIMARY KEY (`id`),
                                         KEY `idx_reports_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `sentence_games` (
                                                `id`             BIGINT       NOT NULL AUTO_INCREMENT,
                                                `created_at`     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                                `modified_at`    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                                                `original_content`  TEXT         NOT NULL,
                                                `corrected_content` TEXT         NOT NULL,
                                                `code`            VARCHAR(50)  NOT NULL,

                                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `translation_tags` (
                                                  `id`       BIGINT       NOT NULL AUTO_INCREMENT,
                                                  `code`     VARCHAR(50)  NOT NULL UNIQUE,
                                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

# 프롬프트 테이블
CREATE TABLE IF NOT EXISTS `prompts` (
                                         `id`            BIGINT       NOT NULL AUTO_INCREMENT,
                                         `created_at`    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                         `modified_at`   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                                         `member_id`     BIGINT       NOT NULL,

                                         `prompt_type`   VARCHAR(20)  NOT NULL,
                                         `title`         VARCHAR(255) NOT NULL,
                                         `content`       TEXT         NOT NULL,
                                         `scenario_id`   VARCHAR(50)  NOT NULL UNIQUE,

                                         PRIMARY KEY (`id`),
                                         FOREIGN KEY (`member_id`) REFERENCES `members`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `learning_notes` (
                                                `id`                BIGINT       NOT NULL AUTO_INCREMENT,
                                                `member_id`         BIGINT       NOT NULL,
                                                `original_content`  VARCHAR(100)         NOT NULL,
                                                `corrected_content` VARCHAR(100)         NOT NULL,
                                                `created_at`    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                                `is_marked`         BOOLEAN      NOT NULL DEFAULT FALSE,

                                                PRIMARY KEY (`id`),
                                                KEY `idx_learning_notes_member_id` (`member_id`),

                                                CONSTRAINT `fk_learning_note_member`
                                                FOREIGN KEY (`member_id`)
                                                REFERENCES `members` (`id`)
                                                ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `feedbacks` (
                                           `id`               BIGINT       NOT NULL AUTO_INCREMENT,
                                           `learning_note_id` BIGINT       NOT NULL,
                                           `created_at`    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                           `tag`              VARCHAR(50)  NOT NULL,
                                           `problem`          VARCHAR(100)         NOT NULL,
                                           `correction`       VARCHAR(100)         NOT NULL,
                                           `extra`            VARCHAR(100)         NOT NULL,

                                           PRIMARY KEY (`id`),
                                           KEY `idx_learning_feedbacks_learning_note_id` (`learning_note_id`),

                                           CONSTRAINT `fk_feedback_note`
                                           FOREIGN KEY (`learning_note_id`)
                                           REFERENCES `learning_notes` (`id`)
                                           ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
