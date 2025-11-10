INSERT INTO member (email, password, nickname, member_role, created_at, updated_at) VALUES
('test1@test.com', '{bcrypt}$2a$10$sjMPbacARshtVQmoM7UMe.3VPjxXcUU8n83R5dWg0rKrvHscqXmKK', 'user1', 'USER', NOW(), NOW()),
('test2@test.com', '{bcrypt}$2a$10$sjMPbacARshtVQmoM7UMe.3VPjxXcUU8n83R5dWg0rKrvHscqXmKK', 'user2', 'USER', NOW(), NOW()),
('test3@test.com', '{bcrypt}$2a$10$sjMPbacARshtVQmoM7UMe.3VPjxXcUU8n83R5dWg0rKrvHscqXmKK', 'user3', 'USER', NOW(), NOW()),
('test4@test.com', '{bcrypt}$2a$10$sjMPbacARshtVQmoM7UMe.3VPjxXcUU8n83R5dWg0rKrvHscqXmKK', 'user4', 'USER', NOW(), NOW()),
('test5@test.com', '{bcrypt}$2a$10$sjMPbacARshtVQmoM7UMe.3VPjxXcUU8n83R5dWg0rKrvHscqXmKK', 'user5', 'USER', NOW(), NOW());
-- {bcrypt}$2a$10$1qchtQbNry5JaJm55B7FfucDpPqeQqp3dBMg6jxsX7BLGZTpJdF0G