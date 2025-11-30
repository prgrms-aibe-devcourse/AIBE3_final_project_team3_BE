-- V11: Chat Members 초기 데이터
-- 채팅방 멤버 매핑 데이터 삽입

INSERT INTO chat_members (member_id, chat_room_id, chat_room_type, last_read_at, last_read_sequence, chat_notification_setting, created_at, modified_at)
VALUES
-- Direct 채팅방 (1번~15번 direct_chat_rooms 기준)
(1, 1, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(2, 1, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(1, 2, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(3, 2, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(1, 3, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(4, 3, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(1, 4, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(5, 4, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(1, 5, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(6, 5, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(1, 6, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(7, 6, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(1, 7, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(8, 7, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(2, 8, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(3, 8, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(2, 9, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(4, 9, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(3, 10, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(5, 10, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(4, 11, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(6, 11, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(5, 12, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(7, 12, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(6, 13, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(8, 13, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(7, 14, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(9, 14, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

(8, 15, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),
(10, 15, 'DIRECT', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- Group Chat Room Members (1~18번 group_chat_rooms)
-- 그룹 1: English Practice Room (owner: 1)
(1, 1, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(2, 1, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(3, 1, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(4, 1, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(5, 1, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 2: Daily Conversation (owner: 2)
(2, 2, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(3, 2, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(4, 2, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 3: Global Culture Talk (owner: 1)
(1, 3, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(5, 3, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(6, 3, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(7, 3, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 4: Travel Stories (owner: 3)
(3, 4, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(8, 4, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(9, 4, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 5: Sports Chat (owner: 2)
(2, 5, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(4, 5, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(6, 5, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(10, 5, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 6: Fitness Buddies (owner: 4)
(4, 6, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(5, 6, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(7, 6, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 7: Book Club (owner: 1)
(1, 7, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(2, 7, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(6, 7, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(8, 7, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 8: Novel Readers (owner: 5)
(5, 8, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(3, 8, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(9, 8, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 9: Game Lovers (owner: 3)
(3, 9, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(4, 9, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(7, 9, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(10, 9, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 10: E-sports Fans (owner: 6)
(6, 10, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(1, 10, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(5, 10, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 11: Cooking Together (owner: 2)
(2, 11, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(3, 11, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(8, 11, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 12: Food Lovers (owner: 7)
(7, 12, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(1, 12, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(4, 12, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(9, 12, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 13: Music & Songs (owner: 1)
(1, 13, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(3, 13, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(5, 13, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(6, 13, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 14: K-pop Fans (owner: 8)
(8, 14, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(2, 14, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(7, 14, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 15: Tech Talk (owner: 3)
(3, 15, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(1, 15, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(4, 15, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(10, 15, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 16: Developers Hub (owner: 9)
(9, 16, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(5, 16, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(6, 16, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 17: Free Talk Room (owner: 1)
(1, 17, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(2, 17, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(3, 17, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(4, 17, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(5, 17, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(6, 17, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),

-- 그룹 18: Casual Hangout (owner: 10)
(10, 18, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(7, 18, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(8, 18, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW()),
(9, 18, 'GROUP', NULL, 0, 'ALWAYS', NOW(), NOW());
