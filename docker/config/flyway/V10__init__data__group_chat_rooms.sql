-- V10: Group Chat Room 초기 데이터
-- 그룹 채팅방 데이터 삽입

INSERT INTO group_chat_rooms (name, description, topic, owner_id, password, current_sequence, created_at, modified_at)
VALUES
-- 언어 교환
('English Practice Room', 'Let''s practice English together! 함께 영어 연습해요.', '언어 교환', 1, NULL, 0, NOW(), NOW()),
('Daily Conversation', 'Share your daily stories in English. 일상을 영어로 나눠요.', '언어 교환', 2, NULL, 0, NOW(), NOW()),

-- 문화 교류
('Global Culture Talk', 'Share your country''s culture! 나라별 문화를 공유해요.', '문화 교류', 1, NULL, 0, NOW(), NOW()),
('Travel Stories', 'Share your travel experiences! 여행 경험을 공유해요.', '문화 교류', 3, NULL, 0, NOW(), NOW()),

-- 운동
('Sports Chat', 'Talk about sports in English. 영어로 스포츠 이야기.', '운동', 2, NULL, 0, NOW(), NOW()),
('Fitness Buddies', 'Share workout tips! 운동 팁과 동기부여.', '운동', 4, NULL, 0, NOW(), NOW()),

-- 독서
('Book Club', 'Discuss books in English. 영어로 책 이야기해요.', '독서', 1, NULL, 0, NOW(), NOW()),
('Novel Readers', 'Share your favorite novels. 좋아하는 소설 공유.', '독서', 5, NULL, 0, NOW(), NOW()),

-- 게임
('Game Lovers', 'Talk about games! 게임 이야기를 영어로.', '게임', 3, NULL, 0, NOW(), NOW()),
('E-sports Fans', 'Discuss e-sports. 이스포츠와 게임 토론.', '게임', 6, NULL, 0, NOW(), NOW()),

-- 요리
('Cooking Together', 'Share recipes in English! 영어로 레시피 공유.', '요리', 2, NULL, 0, NOW(), NOW()),
('Food Lovers', 'Discuss food culture. 음식 문화 이야기.', '요리', 7, NULL, 0, NOW(), NOW()),

-- 음악
('Music & Songs', 'Learn English through music! 음악으로 영어 배우기.', '음악', 1, NULL, 0, NOW(), NOW()),
('K-pop Fans', 'Talk about K-pop and pop music. K-pop과 팝 음악 이야기.', '음악', 8, NULL, 0, NOW(), NOW()),

-- IT
('Tech Talk', 'Technology discussions. 기술과 프로그래밍 이야기.', 'IT', 3, NULL, 0, NOW(), NOW()),
('Developers Hub', 'Share coding tips. 코딩 팁과 기술 뉴스.', 'IT', 9, NULL, 0, NOW(), NOW()),

-- 자유
('Free Talk Room', 'Chat about anything! 아무 주제나 자유롭게.', '자유', 1, NULL, 0, NOW(), NOW()),
('Casual Hangout', 'Just relax and chat. 편하게 수다 떨어요.', '자유', 10, NULL, 0, NOW(), NOW());
