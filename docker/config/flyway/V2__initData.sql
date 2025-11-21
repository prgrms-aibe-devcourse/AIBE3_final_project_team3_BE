USE mysql_db;

-- 패스워드 test1234
INSERT INTO members (
    email, password, name, nickname, country,
    interests, english_level, description, role,
    membership_grade, last_sign_in_at, is_blocked, blocked_at,
    is_deleted, deleted_at, block_reason, profile_image_url,
    created_at, modified_at
) VALUES
-- 1
('test1@test.com', '$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Alice', 'Ailee', 'KR',
 '["📚 reading", "🎵 music"]',
 'BEGINNER', '테스트 유저1', 'ROLE_MEMBER', 'BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 2
('test2@test.com', '$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Bob', 'Bobby', 'US',
 '["⚽ sports", "🎮 gaming"]',
 'INTERMEDIATE', '테스트 유저2', 'ROLE_MEMBER', 'BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 3
('test3@test.com', '$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Cathy','Cat','JP',
 '["🍣 sushi", "📷 photography"]',
 'ADVANCED','테스트 유저3','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 4
('test4@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'David','Dave','KR',
 '["🏋️ fitness","🎧 hiphop"]',
 'BEGINNER','테스트 유저4','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 5
('test5@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Emma','Em','FR',
 '["🎨 art","🧁 baking"]',
 'INTERMEDIATE','테스트 유저5','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 6
('test6@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Frank','Franky','UK',
 '["🚴 cycling","🎬 movies"]',
 'ADVANCED','테스트 유저6','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 7
('test7@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Grace','Gracie','CA',
 '["📚 study","🎤 singing"]',
 'BEGINNER','테스트 유저7','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 8
('test8@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Henry','Hen','AU',
 '["🐶 dogs","🚗 cars"]',
 'INTERMEDIATE','테스트 유저8','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 9
('test9@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Irene','Ivy','KR',
 '["💄 fashion","📚 study"]',
 'BEGINNER','테스트 유저9','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 10
('test10@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Jake','JJ','US',
 '["🏀 basketball","🎮 gaming"]',
 'ADVANCED','테스트 유저10','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 11
('test11@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Kate','K','SE',
 '["🌲 nature","📸 camera"]',
 'INTERMEDIATE','테스트 유저11','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 12
('test12@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Leo','Leon','BR',
 '["🥁 drums","⚽ soccer"]',
 'BEGINNER','테스트 유저12','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 13
('test13@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Mia','Mimi','KR',
 '["🧋 bubble tea","🧘 yoga"]',
 'ADVANCED','테스트 유저13','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 14
('test14@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Nate','Nat','JP',
 '["🎧 lo-fi","🎮 RPG games"]',
 'INTERMEDIATE','테스트 유저14','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 15 (신고당한 유저)
('test15@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Olivia','Liv','CA',
 '["🍁 canada","🏞 hiking"]',
 'BEGINNER','테스트 유저15','ROLE_MEMBER','BASIC',
 NOW(), TRUE, NOW(), FALSE, NULL, 'SPAM', NULL, NOW(), NOW()),

-- 16 (차단된 유저)
('test16@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Paul','P','DE',
 '["🎸 guitar","🍺 beer"]',
 'ADVANCED','테스트 유저16','ROLE_MEMBER','BASIC',
 NOW(), TRUE, NOW(), FALSE, NULL, 'ABUSE', NULL, NOW(), NOW()),

-- 17
('test17@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Queen','Q','KR',
 '["💃 dance","🍜 ramen"]',
 'INTERMEDIATE','테스트 유저17','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 18
('test18@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Ryan','Ry','US',
 '["💻 coding","🎨 design"]',
 'ADVANCED','테스트 유저18','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 19 (프리미엄)
('test19@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Sophie','Soso','FR',
 '["🥐 croissant","🎻 violin"]',
 'BEGINNER','테스트 유저19','ROLE_MEMBER','PREMIUM',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 20 (관리자)
('test20@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Tom','Tommy','KR',
 '["🚀 space","⚙️ robots"]',
 'INTERMEDIATE','테스트 유저20','ROLE_ADMIN','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW());

INSERT INTO friendships (smaller_member_id, larger_member_id, created_at)
VALUES
    (1, 2, NOW()),
    (1, 3, NOW()),
    (1, 4, NOW()),
    (1, 5, NOW()),
    (1, 6, NOW());

INSERT INTO friendship_requests (sender_id, receiver_id, created_at)
VALUES
    (1, 7, NOW()),
    (1, 8, NOW()),
    (1, 9, NOW()),
    (10, 1, NOW()),
    (11, 1, NOW()),
    (12, 1, NOW());
;

INSERT INTO notifications (receiver_id, sender_id, type, content, is_read, created_at)
VALUES
-- ===========================
-- # Receiver: 1번 (총 10개)
-- ===========================
-- CHAT_MESSAGE
(1, 2, 'CHAT_MESSAGE', 'Hey, u free later?', FALSE, NOW()),
(1, 3, 'CHAT_MESSAGE', '오늘 저녁 ok?', TRUE, NOW()),
(1, 4, 'CHAT_MESSAGE', 'I sent u the file!', FALSE, NOW()),
(1, 5, 'CHAT_MESSAGE', '요즘 뭐해? long time no see lol', TRUE, NOW()),
(1, 6, 'CHAT_MESSAGE', 'Call 가능? need to talk quick', FALSE, NOW()),
-- FRIEND REQUEST / ACCEPT / REJECT (content X)
(1, 7, 'FRIEND_REQUEST', NULL, FALSE, NOW()),
(1, 8, 'FRIEND_REQUEST', NULL, TRUE, NOW()),
(1, 10, 'FRIEND_REQUEST_ACCEPT', NULL, FALSE, NOW()),
(1, 11, 'FRIEND_REQUEST_REJECT', NULL, TRUE, NOW()),
-- SYSTEM_ALERT (content O)
(1, NULL, 'SYSTEM_ALERT', 'System update completed successfully.', FALSE, NOW()),

-- ===========================
-- # Receiver: 2번 (총 7개)
-- ===========================
-- CHAT_MESSAGE
(2, 1, 'CHAT_MESSAGE', 'Dinner? sushi maybe?', FALSE, NOW()),
(2, 3, 'CHAT_MESSAGE', '문서 봤어! looks good.', TRUE, NOW()),
-- FRIEND REQUEST / ACCEPT / REJECT
(2, 4, 'FRIEND_REQUEST', NULL, FALSE, NOW()),
(2, 5, 'FRIEND_REQUEST_ACCEPT', NULL, TRUE, NOW()),
(2, 6, 'FRIEND_REQUEST_REJECT', NULL, FALSE, NOW()),
-- SYSTEM_ALERT
(2, NULL, 'SYSTEM_ALERT', 'New features are now available.', TRUE, NOW()),
-- INVITATION
(2, 7, 'CHAT_INVITATION', NULL, FALSE, NOW()),

-- ===========================
-- # Receiver: 3번 (총 7개)
-- ===========================
-- CHAT_MESSAGE
(3, 1, 'CHAT_MESSAGE', 'Meeting today 3pm ok?', TRUE, NOW()),
(3, 2, 'CHAT_MESSAGE', '자료 좀 확인해줘 plz!', FALSE, NOW()),
-- FRIEND REQUEST / ACCEPT / REJECT
(3, 4, 'FRIEND_REQUEST', NULL, FALSE, NOW()),
(3, 5, 'FRIEND_REQUEST_ACCEPT', NULL, TRUE, NOW()),
(3, 6, 'FRIEND_REQUEST_REJECT', NULL, FALSE, NOW()),
-- SYSTEM_ALERT
(3, NULL, 'SYSTEM_ALERT', 'Security update required.', FALSE, NOW()),
-- INVITATION
(3, 8, 'CHAT_INVITATION', NULL, TRUE, NOW());





