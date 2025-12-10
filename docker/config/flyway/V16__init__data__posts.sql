-- 게시글 초기 데이터
USE `mysql_db`;

-- 게시글 샘플 데이터
INSERT INTO `posts` (`id`, `member_id`, `title`, `content`, `view_count`, `created_at`, `modified_at`)
VALUES
    (1, 1, 'Welcome to MixChat Community!',
     'Hello everyone! This is the first post in our community. Feel free to share your language learning experiences and tips here. Let''s help each other grow!',
     150, '2024-11-01 10:00:00', '2024-11-01 10:00:00'),

    (2, 2, 'Tips for Learning English Effectively',
     'I''ve been learning English for 5 years, and here are my top tips:\n1. Practice speaking daily\n2. Watch movies with subtitles\n3. Read English books\n4. Use language exchange apps\n5. Don''t be afraid to make mistakes!',
     230, '2024-11-05 14:30:00', '2024-11-05 14:30:00'),

    (3, 3, 'Anyone interested in a study group?',
     'Looking for people to practice English conversation. We can meet online twice a week. Intermediate level preferred. Comment if you''re interested!',
     85, '2024-11-10 09:15:00', '2024-11-10 09:15:00'),

    (4, 4, 'My Journey from Beginner to Advanced',
     'Started learning English 3 years ago with zero knowledge. Today I can confidently communicate in English! Here''s my story and what helped me the most...',
     420, '2024-11-15 16:45:00', '2024-11-15 16:45:00'),

    (5, 5, 'Recommended English Learning Resources',
     'Here are some amazing free resources I''ve found:\n- BBC Learning English\n- Duolingo\n- English Central\n- TED Talks\n- Grammarly\n\nWhat are your favorites?',
     195, '2024-11-20 11:20:00', '2024-11-20 11:20:00'),

    (6, 1, 'Common Mistakes Korean Speakers Make',
     'As a native English speaker teaching Korean students, I''ve noticed these common mistakes:\n- Pronunciation of "R" and "L"\n- Using articles (a, an, the)\n- Verb tenses\nLet me explain each one...',
     310, '2024-11-25 13:00:00', '2024-11-25 13:00:00'),

    (7, 2, 'Best English Podcasts for Learners',
     'Podcasts are great for improving listening skills! Here are my top picks:\n1. All Ears English\n2. 6 Minute English by BBC\n3. The English We Speak\n4. Luke''s English Podcast\n5. ESL Pod',
     175, '2024-11-28 15:30:00', '2024-11-28 15:30:00'),

    (8, 6, 'How I Improved My Speaking Skills',
     'I was terrified of speaking English before, but I overcame it! Here''s what worked for me:\n- Talking to myself in English\n- Recording my voice\n- Joining conversation clubs\n- Using speech shadowing technique',
     260, '2024-12-01 10:45:00', '2024-12-01 10:45:00'),

    (9, 7, 'English Idioms You Should Know',
     'Learning idioms makes you sound more natural! Here are 10 essential idioms:\n1. Break the ice\n2. Hit the books\n3. Cost an arm and a leg\n4. A piece of cake\n5. Let the cat out of the bag\n...and more!',
     190, '2024-12-02 14:00:00', '2024-12-02 14:00:00'),

    (10, 8, 'Question: How to prepare for TOEIC?',
     'I need to take TOEIC next month for my job. Any study tips or recommended materials? My current level is intermediate. Thanks in advance!',
     125, '2024-12-03 09:30:00', '2024-12-03 09:30:00');

