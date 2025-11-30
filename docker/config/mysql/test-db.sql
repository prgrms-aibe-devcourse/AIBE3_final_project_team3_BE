CREATE DATABASE IF NOT EXISTS mysql_test
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

GRANT ALL PRIVILEGES ON mysql_test.* TO 'mixchat_user'@'%';