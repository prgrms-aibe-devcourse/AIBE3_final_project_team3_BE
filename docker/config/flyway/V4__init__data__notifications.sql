INSERT INTO notifications (receiver_id, sender_id, type, content, is_read, created_at)
VALUES
-- # Receiver: 1번 (총 40개)

-- 초기 6개
(1, 2,  'CHAT_MESSAGE', 'Did you see the 최근 news? It was so Interesting', FALSE, NOW() - INTERVAL 40 MINUTE),
(1, 3,  'FRIEND_REQUEST', NULL, FALSE, NOW() - INTERVAL 39 MINUTE),
(1, 4,  'FRIEND_REQUEST_ACCEPT', NULL, TRUE, NOW() - INTERVAL 38 MINUTE),
(1, 5,  'FRIEND_REQUEST_REJECT', NULL, TRUE, NOW() - INTERVAL 37 MINUTE),
(1, 6,  'CHAT_INVITATION', NULL, FALSE, NOW() - INTERVAL 36 MINUTE),
(1, NULL,'SYSTEM_ALERT', '시스템 초기 알림: 개인정보 보호 설정 확인. Check your privacy settings.', FALSE, NOW() - INTERVAL 35 MINUTE),

-- CHAT_MESSAGE (14개)
(1, 7,  'CHAT_MESSAGE', 'Hey, u free later? 오늘 저녁에 시간 돼?', FALSE, NOW() - INTERVAL 34 MINUTE),
(1, 8,  'CHAT_MESSAGE', '회의 자료 다 봤어! looks good.', TRUE, NOW() - INTERVAL 33 MINUTE),
(1, 9,  'CHAT_MESSAGE', 'I sent u the file! 파일 확인해 줘!', FALSE, NOW() - INTERVAL 32 MINUTE),
(1, 10, 'CHAT_MESSAGE', '요즘 뭐해? long time no see lol. 잘 지내?', TRUE, NOW() - INTERVAL 31 MINUTE),
(1, 11, 'CHAT_MESSAGE', 'Call 가능? 5분만 통화할 수 있을까?', FALSE, NOW() - INTERVAL 30 MINUTE),
(1, 12, 'CHAT_MESSAGE', 'New project idea meeting tomorrow? 내일 회의 시간에 보자.', FALSE, NOW() - INTERVAL 29 MINUTE),
(1, 13, 'CHAT_MESSAGE', 'Can you review my PR? PR 리뷰 요청했어.', TRUE, NOW() - INTERVAL 28 MINUTE),
(1, 14, 'CHAT_MESSAGE', 'Did you see the latest news? 그거 정말 충격적이더라.', FALSE, NOW() - INTERVAL 27 MINUTE),
(1, 15, 'CHAT_MESSAGE', 'Lunch team building event? 점심에 팀 빌딩 이벤트 같이 하자!', TRUE, NOW() - INTERVAL 26 MINUTE),
(1, 16, 'CHAT_MESSAGE', 'Urgent bug fix needed! 긴급 버그 수정 필요해!', FALSE, NOW() - INTERVAL 25 MINUTE),
(1, 17, 'CHAT_MESSAGE', 'Don''t forget the deadline! 마감 기한 잊지 마세요.', TRUE, NOW() - INTERVAL 24 MINUTE),
(1, 18, 'CHAT_MESSAGE', 'Let''s grab coffee later. 나중에 커피 한 잔 할까?', FALSE, NOW() - INTERVAL 23 MINUTE),
(1, 19, 'CHAT_MESSAGE', 'Check out this funny meme. 완전 웃긴 밈이야.', TRUE, NOW() - INTERVAL 22 MINUTE),
(1, 20, 'CHAT_MESSAGE', 'Feedback on the design, please. 디자인 피드백 부탁해.', FALSE, NOW() - INTERVAL 21 MINUTE),

-- FRIEND REQUEST / ACCEPT / REJECT (11개)
(1, 21, 'FRIEND_REQUEST', NULL, FALSE, NOW() - INTERVAL 20 MINUTE),
(1, 22, 'FRIEND_REQUEST', NULL, TRUE, NOW() - INTERVAL 19 MINUTE),
(1, 23, 'FRIEND_REQUEST_ACCEPT', NULL, FALSE, NOW() - INTERVAL 18 MINUTE),
(1, 24, 'FRIEND_REQUEST_ACCEPT', NULL, TRUE, NOW() - INTERVAL 17 MINUTE),
(1, 25, 'FRIEND_REQUEST_REJECT', NULL, FALSE, NOW() - INTERVAL 16 MINUTE),
(1, 26, 'FRIEND_REQUEST', NULL, FALSE, NOW() - INTERVAL 15 MINUTE),
(1, 27, 'FRIEND_REQUEST_ACCEPT', NULL, TRUE, NOW() - INTERVAL 14 MINUTE),
(1, 28, 'FRIEND_REQUEST', NULL, FALSE, NOW() - INTERVAL 13 MINUTE),
(1, 29, 'FRIEND_REQUEST_REJECT', NULL, TRUE, NOW() - INTERVAL 12 MINUTE),
(1, 30, 'FRIEND_REQUEST_ACCEPT', NULL, FALSE, NOW() - INTERVAL 11 MINUTE),
(1, 31, 'FRIEND_REQUEST_REJECT', NULL, FALSE, NOW() - INTERVAL 10 MINUTE),

-- SYSTEM_ALERT (5개)
(1, NULL, 'SYSTEM_ALERT', 'Your session will expire soon. 세션이 곧 만료됩니다.', FALSE, NOW() - INTERVAL 9 MINUTE),
(1, NULL, 'SYSTEM_ALERT', 'New policy update. 새로운 정책이 업데이트되었습니다.', TRUE, NOW() - INTERVAL 8 MINUTE),
(1, NULL, 'SYSTEM_ALERT', 'Maintenance scheduled for tonight. 오늘 밤 시스템 점검이 예정되어 있습니다.', FALSE, NOW() - INTERVAL 7 MINUTE),
(1, NULL, 'SYSTEM_ALERT', 'Congratulations! You leveled up. 축하합니다! 레벨이 상승했습니다.', TRUE, NOW() - INTERVAL 6 MINUTE),
(1, NULL, 'SYSTEM_ALERT', 'Please verify your account information. 계정 정보를 확인해 주세요.', FALSE, NOW() - INTERVAL 5 MINUTE),

-- CHAT_INVITATION (4개)
(1, 32, 'CHAT_INVITATION', NULL, FALSE, NOW() - INTERVAL 4 MINUTE),
(1, 33, 'CHAT_INVITATION', NULL, TRUE, NOW() - INTERVAL 3 MINUTE),
(1, 34, 'CHAT_INVITATION', NULL, FALSE, NOW() - INTERVAL 2 MINUTE),
(1, 35, 'CHAT_INVITATION', NULL, TRUE, NOW() - INTERVAL 1 MINUTE),



-- Receiver: 2번 (총 7개)
(2, 1, 'CHAT_MESSAGE', 'Dinner? sushi maybe?', FALSE, NOW()),
(2, 3, 'CHAT_MESSAGE', '문서 봤어! looks good.', TRUE, NOW()),
(2, 4, 'FRIEND_REQUEST', NULL, FALSE, NOW()),
(2, 5, 'FRIEND_REQUEST_ACCEPT', NULL, TRUE, NOW()),
(2, 6, 'FRIEND_REQUEST_REJECT', NULL, FALSE, NOW()),
(2, NULL, 'SYSTEM_ALERT', 'New features are now available.', TRUE, NOW()),
(2, 7, 'CHAT_INVITATION', NULL, FALSE, NOW()),

-- Receiver: 3번 (총 7개)
(3, 1, 'CHAT_MESSAGE', 'Meeting today 3pm ok?', TRUE, NOW()),
(3, 2, 'CHAT_MESSAGE', '자료 좀 확인해줘 plz!', FALSE, NOW()),
(3, 4, 'FRIEND_REQUEST', NULL, FALSE, NOW()),
(3, 5, 'FRIEND_REQUEST_ACCEPT', NULL, TRUE, NOW()),
(3, 6, 'FRIEND_REQUEST_REJECT', NULL, FALSE, NOW()),
(3, NULL, 'SYSTEM_ALERT', 'Security update required.', FALSE, NOW()),
(3, 8, 'CHAT_INVITATION', NULL, TRUE, NOW());
