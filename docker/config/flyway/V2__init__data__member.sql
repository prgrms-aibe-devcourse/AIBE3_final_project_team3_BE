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
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 21
('test21@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
    'Uma','Umi','JP',
    '["🎮 gaming","🍣 sushi"]',
    'INTERMEDIATE','테스트 유저21','ROLE_MEMBER','BASIC',
    NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 22
('test22@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Victor','Vic','US',
 '["🏈 football","🎧 music"]',
 'BEGINNER','테스트 유저22','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 23
('test23@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Wendy','Wen','KR',
 '["🧋 bubble tea","📚 reading"]',
 'ADVANCED','테스트 유저23','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 24
('test24@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Xavier','Xav','CA',
 '["🎬 movies","🎤 singing"]',
 'INTERMEDIATE','테스트 유저24','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 25
('test25@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Yuna','Yuni','KR',
 '["📷 photography","🧘 yoga"]',
 'BEGINNER','테스트 유저25','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 26
('test26@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Zack','Zed','AU',
 '["🚗 cars","🐶 dogs"]',
 'INTERMEDIATE','테스트 유저26','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 27
('test27@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Amber','Amb','FR',
 '["🥐 pastries","🎨 art"]',
 'ADVANCED','테스트 유저27','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 28
('test28@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Brian','Bri','UK',
 '["🚴 cycling","🎧 EDM"]',
 'BEGINNER','테스트 유저28','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 29
('test29@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Clara','Cla','ES',
 '["💃 dance","🍷 wine"]',
 'INTERMEDIATE','테스트 유저29','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 30
('test30@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Dylan','Dyl','KR',
 '["🏸 badminton","🎮 FPS games"]',
 'ADVANCED','테스트 유저30','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 31
('test31@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Ella','Elle','IT',
 '["🍝 pasta","🏖 travel"]',
 'INTERMEDIATE','테스트 유저31','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 32
('test32@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Finn','F','US',
 '["🏀 basketball","📚 comics"]',
 'BEGINNER','테스트 유저32','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 33
('test33@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Gina','G','CA',
 '["🍁 maple","🎵 pop"]',
 'ADVANCED','테스트 유저33','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 34
('test34@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Hank','HK','NZ',
 '["🏉 rugby","🎬 drama"]',
 'INTERMEDIATE','테스트 유저34','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 35
('test35@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Isla','Is','KR',
 '["📖 novels","📷 portraits"]',
 'BEGINNER','테스트 유저35','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 36
('test36@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Jay','J','US',
 '["🎧 rock","🎮 RPG"]',
 'ADVANCED','테스트 유저36','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 37
('test37@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Kara','Kar','DE',
 '["🍺 beer","🎸 rock"]',
 'INTERMEDIATE','테스트 유저37','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 38
('test38@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Liam','Lia','JP',
 '["🍣 omakase","🎤 karaoke"]',
 'BEGINNER','테스트 유저38','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 39
('test39@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Molly','Mol','US',
 '["🎨 sketch","🏕 camping"]',
 'ADVANCED','테스트 유저39','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 40
('test40@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Noah','N','KR',
 '["🍜 ramen","📝 studying"]',
 'INTERMEDIATE','테스트 유저40','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 41
('test41@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Olga','Olg','PL',
 '["🎿 ski","🧘 meditation"]',
 'BEGINNER','테스트 유저41','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 42
('test42@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Peter','Pete','US',
 '["🏎 F1","🎧 hiphop"]',
 'INTERMEDIATE','테스트 유저42','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 43
('test43@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Quinn','Qin','KR',
 '["🧋 milk tea","📚 books"]',
 'ADVANCED','테스트 유저43','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 44
('test44@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Rose','Ro','FR',
 '["🍷 wine","🎨 painting"]',
 'BEGINNER','테스트 유저44','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 45
('test45@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Sam','S','ES',
 '["⚽ soccer","🎮 arcade"]',
 'INTERMEDIATE','테스트 유저45','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 46
('test46@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Tina','Ti','KR',
 '["💄 beauty","📷 vlog"]',
 'ADVANCED','테스트 유저46','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 47
('test47@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Ursula','Ur','US',
 '["🎤 singing","🎬 thriller"]',
 'BEGINNER','테스트 유저47','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 48
('test48@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Vera','Ve','JP',
 '["🍣 sashimi","🎧 pop"]',
 'INTERMEDIATE','테스트 유저48','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 49
('test49@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Will','Wil','CA',
 '["📷 camera","☕ coffee"]',
 'ADVANCED','테스트 유저49','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 50
('test50@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Yuri','Yu','KR',
 '["🧘 yoga","🎮 puzzle games"]',
 'BEGINNER','테스트 유저50','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 51
('test51@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Aaron','A','US',
 '["🏀 basketball","🎧 rap"]',
 'INTERMEDIATE','테스트 유저51','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 52
('test52@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Bella','Bel','FR',
 '["🥐 baking","🧵 knitting"]',
 'ADVANCED','테스트 유저52','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 53
('test53@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Cody','Cod','JP',
 '["🎮 rhythm games","📚 manga"]',
 'BEGINNER','테스트 유저53','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 54
('test54@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Dana','Dan','UK',
 '["🎬 sci-fi","🎨 sketch"]',
 'INTERMEDIATE','테스트 유저54','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 55
('test55@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Eli','El','KR',
 '["📷 portrait","🎧 jazz"]',
 'ADVANCED','테스트 유저55','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 56
('test56@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Faye','Fy','US',
 '["🎤 karaoke","💄 makeup"]',
 'BEGINNER','테스트 유저56','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 57
('test57@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Gavin','Gav','CA',
 '["🏒 hockey","🎮 FPS"]',
 'INTERMEDIATE','테스트 유저57','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 58
('test58@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Hazel','Haz','KR',
 '["🧋 tea","📖 poems"]',
 'ADVANCED','테스트 유저58','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 59
('test59@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Ian','I','DE',
 '["🍺 beer","⚽ Bundesliga"]',
 'BEGINNER','테스트 유저59','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 60
('test60@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Judy','Jud','FR',
 '["🎼 classical","🧁 cakes"]',
 'INTERMEDIATE','테스트 유저60','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 61
('test61@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Kyle','Ky','KR',
 '["🍜 ramen","🎮 action"]',
 'ADVANCED','테스트 유저61','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 62
('test62@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Lara','Lar','JP',
 '["🍱 lunchbox","🧘 mindfulness"]',
 'BEGINNER','테스트 유저62','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 63
('test63@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Mason','Mas','US',
 '["🏈 rugby","📚 study"]',
 'INTERMEDIATE','테스트 유저63','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 64
('test64@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Nina','Ni','CA',
 '["📷 film","🐱 cats"]',
 'ADVANCED','테스트 유저64','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 65
('test65@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Oscar','Osc','KR',
 '["📚 lecture","🎧 R&B"]',
 'BEGINNER','테스트 유저65','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 66
('test66@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Poppy','Pop','US',
 '["📖 diary","🎤 singing"]',
 'INTERMEDIATE','테스트 유저66','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 67
('test67@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Quentin','Quen','FR',
 '["🍞 bakery","🎬 noir films"]',
 'ADVANCED','테스트 유저67','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 68
('test68@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Ruby','R','JP',
 '["🍡 dessert","📚 study"]',
 'BEGINNER','테스트 유저68','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 69
('test69@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Sean','Sea','US',
 '["🎮 strategy games","🎧 metal"]',
 'INTERMEDIATE','테스트 유저69','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 70
('test70@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Tori','Tor','CA',
 '["❄️ snow","🎨 modern art"]',
 'ADVANCED','테스트 유저70','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 71
('test71@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Uri','Ur','UK',
 '["🧘 stretching","🌄 trekking"]',
 'BEGINNER','테스트 유저71','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 72
('test72@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Violet','Vio','JP',
 '["🍱 bento","🎧 chill"]',
 'INTERMEDIATE','테스트 유저72','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 73
('test73@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Walt','Wa','KR',
 '["🍜 udon","📷 DSLR"]',
 'ADVANCED','테스트 유저73','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 74
('test74@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Xena','Xe','US',
 '["🎤 ballad","🛍 fashion"]',
 'BEGINNER','테스트 유저74','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 75
('test75@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Yosef','Yo','DE',
 '["🍺 German beer","🧗 climbing"]',
 'INTERMEDIATE','테스트 유저75','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 76
('test76@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Zelda','Zel','CA',
 '["📷 urban","🎮 open world"]',
 'ADVANCED','테스트 유저76','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 77
('test77@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Aron','Ar','US',
 '["🏈 NFL","📺 sitcoms"]',
 'BEGINNER','테스트 유저77','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 78
('test78@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Bea','Be','UK',
 '["📚 classics","🌧 rainy days"]',
 'INTERMEDIATE','테스트 유저78','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 79
('test79@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Chad','Ch','JP',
 '["🍜 tonkotsu","🎧 house"]',
 'ADVANCED','테스트 유저79','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 80
('test80@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Dora','Do','KR',
 '["🧋 tea","🧘 stretching"]',
 'BEGINNER','테스트 유저80','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 81
('test81@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Evan','Ev','US',
 '["🎤 rap","🍕 pizza"]',
 'INTERMEDIATE','테스트 유저81','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 82
('test82@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Fiona','Fio','KR',
 '["📖 romance","🎵 pop"]',
 'ADVANCED','테스트 유저82','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 83
('test83@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Gordon','Gor','CA',
 '["🐻 wildlife","🏕 outdoor"]',
 'BEGINNER','테스트 유저83','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 84
('test84@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Holly','Hol','JP',
 '["🍣 sashimi","🎤 karaoke"]',
 'INTERMEDIATE','테스트 유저84','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 85
('test85@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Ivan','Iv','UK',
 '["🎮 racing","🎧 indie"]',
 'ADVANCED','테스트 유저85','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 86
('test86@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Jenny','Jen','KR',
 '["📝 diary","📸 hobby"]',
 'BEGINNER','테스트 유저86','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 87
('test87@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Kevin','Kev','CA',
 '["🏒 hockey","🎧 dance"]',
 'INTERMEDIATE','테스트 유저87','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 88
('test88@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Lara','La','US',
 '["📚 education","🧋 latte"]',
 'ADVANCED','테스트 유저88','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 89
('test89@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Monica','Mon','FR',
 '["🍮 dessert","🎵 classic"]',
 'BEGINNER','테스트 유저89','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 90
('test90@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Nathan','Nath','JP',
 '["🎮 adventure","🎧 jpop"]',
 'INTERMEDIATE','테스트 유저90','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 91
('test91@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Olive','Oli','KR',
 '["🍰 cakes","📷 travel"]',
 'ADVANCED','테스트 유저91','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 92
('test92@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Pauline','Pau','US',
 '["🎤 vocal","🎬 romance"]',
 'BEGINNER','테스트 유저92','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 93
('test93@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Quincy','Qcy','CA',
 '["🏒 skating","🎵 EDM"]',
 'INTERMEDIATE','테스트 유저93','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 94
('test94@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Rachel','Rac','FR',
 '["🥖 bread","🎨 acrylic"]',
 'ADVANCED','테스트 유저94','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 95
('test95@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Simon','Sim','JP',
 '["🍱 sushi","🎧 lo-fi"]',
 'BEGINNER','테스트 유저95','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 96
('test96@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Tanya','Tan','KR',
 '["🍜 noodle","🎬 thriller"]',
 'INTERMEDIATE','테스트 유저96','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 97
('test97@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Ugo','Ug','IT',
 '["🍕 pizza","📷 cathedral"]',
 'ADVANCED','테스트 유저97','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 98
('test98@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Vince','Vin','US',
 '["🏈 sports","🎮 MOBA"]',
 'BEGINNER','테스트 유저98','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 99
('test99@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Willow','Will','CA',
 '["🎵 singing","📚 fantasy"]',
 'INTERMEDIATE','테스트 유저99','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW()),

-- 100
('test100@test.com','$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
 'Xander','Xan','KR',
 '["🎮 sandbox","🍗 chicken"]',
 'ADVANCED','테스트 유저100','ROLE_MEMBER','BASIC',
 NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW());
