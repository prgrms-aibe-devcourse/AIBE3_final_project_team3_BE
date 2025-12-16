-- V9: Direct Chat Room 초기 데이터
-- 1:1 채팅방 데이터 삽입

INSERT INTO direct_chat_rooms (user1_id, user2_id, created_at, modified_at)
VALUES
-- 1번 회원과의 채팅방들
(1, 2, NOW(), NOW()),
(1, 3, NOW(), NOW()),
(1, 4, NOW(), NOW()),
(1, 5, NOW(), NOW()),
(1, 6, NOW(), NOW()),
(1, 7, NOW(), NOW()),
(1, 8, NOW(), NOW()),

-- 다른 회원들 간의 채팅방
(2, 3, NOW(), NOW()),
(2, 4, NOW(), NOW()),
(3, 5, NOW(), NOW()),
(4, 6, NOW(), NOW()),
(5, 7, NOW(), NOW()),
(6, 8, NOW(), NOW()),
(7, 9, NOW(), NOW()),
(8, 10, NOW(), NOW());
