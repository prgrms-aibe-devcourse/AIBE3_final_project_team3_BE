SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';

CREATE SCHEMA IF NOT EXISTS `mysql_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
CREATE SCHEMA IF NOT EXISTS `mysql_test` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `mysql_db` ;

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


CREATE TABLE IF NOT EXISTS `reports` (
                                         `id`              BIGINT       NOT NULL AUTO_INCREMENT,
                                         `created_at`      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `modified_at`     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    `target_content`  TEXT         NULL,
    `target_member_id` BIGINT      NOT NULL,
    `status`          VARCHAR(10)  NOT NULL,
    `category`          VARCHAR(20) NOT NULL,
    `reason_text`     VARCHAR(255) NULL,

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

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;