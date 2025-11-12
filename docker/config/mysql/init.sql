CREATE USER IF NOT EXISTS 'mixchat_user'@'%' IDENTIFIED BY 'qwer1234';
GRANT ALL PRIVILEGES ON `mysql_db`.* TO 'mixchat_user'@'%';
GRANT ALL PRIVILEGES ON `mysql_test`.* TO 'mixchat_user'@'%';

CREATE SCHEMA IF NOT EXISTS `mysql_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
CREATE SCHEMA IF NOT EXISTS `mysql_test` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `mysql_db` ;

FLUSH PRIVILEGES;