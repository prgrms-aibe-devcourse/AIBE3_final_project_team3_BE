CREATE USER IF NOT EXISTS 'mixchat_user'@'%' IDENTIFIED BY 'qwer1234';
GRANT ALL PRIVILEGES ON `mysql_db`.* TO 'mixchat_user'@'%';
FLUSH PRIVILEGES;