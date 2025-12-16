-- 게시글 테이블의 modified_at 컬럼에서 ON UPDATE CURRENT_TIMESTAMP 속성 제거
USE `mysql_db`;

ALTER TABLE `posts`
MODIFY COLUMN `modified_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

