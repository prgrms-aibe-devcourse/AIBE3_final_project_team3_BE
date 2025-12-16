-- 댓글 초기 데이터
USE `mysql_db`;

-- 댓글 샘플 데이터 (부모 댓글)
INSERT INTO `comments` (`id`, `member_id`, `post_id`, `parent_id`, `content`, `created_at`, `modified_at`)
VALUES
    -- Post 1 comments
    (1, 2, 1, NULL, 'Thanks for creating this community! Looking forward to learning together.',
     '2024-11-01 11:30:00', '2024-11-01 11:30:00'),
    (2, 3, 1, NULL, 'Great initiative! I''m excited to be part of this.',
     '2024-11-01 12:30:00', '2024-11-01 12:30:00'),

    -- Post 2 comments
    (3, 1, 2, NULL, 'These are excellent tips! I especially agree with #5 - making mistakes is how we learn.',
     '2024-11-05 15:30:00', '2024-11-05 15:30:00'),
    (4, 4, 2, NULL, 'Do you have any specific movie recommendations for beginners?',
     '2024-11-05 16:30:00', '2024-11-05 16:30:00'),
    (5, 5, 2, NULL, 'I''ve been doing most of these! Great to know I''m on the right track.',
     '2024-11-05 17:30:00', '2024-11-05 17:30:00'),

    -- Post 3 comments
    (6, 4, 3, NULL, 'I''m interested! What time zone are you in?',
     '2024-11-10 10:00:00', '2024-11-10 10:00:00'),
    (7, 5, 3, NULL, 'Count me in! I need someone to practice with.',
     '2024-11-10 11:00:00', '2024-11-10 11:00:00'),

    -- Post 4 comments
    (8, 1, 4, NULL, 'This is so inspiring! Congratulations on your progress!',
     '2024-11-15 17:30:00', '2024-11-15 17:30:00'),
    (9, 2, 4, NULL, 'Could you share more details about your daily routine?',
     '2024-11-15 18:30:00', '2024-11-15 18:30:00'),
    (10, 5, 4, NULL, 'Amazing journey! You give me hope that I can do it too.',
     '2024-11-15 19:30:00', '2024-11-15 19:30:00'),

    -- Post 5 comments
    (11, 2, 5, NULL, 'Thanks for sharing! I didn''t know about English Central.',
     '2024-11-20 12:30:00', '2024-11-20 12:30:00'),
    (12, 3, 5, NULL, 'I also recommend Rachel''s English on YouTube!',
     '2024-11-20 13:30:00', '2024-11-20 13:30:00'),

    -- Post 6 comments
    (13, 2, 6, NULL, 'This is so helpful! I always struggle with articles.',
     '2024-11-25 14:30:00', '2024-11-25 14:30:00'),
    (14, 3, 6, NULL, 'Could you explain more about when to use "the"?',
     '2024-11-25 15:30:00', '2024-11-25 15:30:00'),

    -- Post 7 comments
    (15, 1, 7, NULL, 'Great list! I''ve been listening to 6 Minute English every day.',
     '2024-11-28 16:30:00', '2024-11-28 16:30:00'),
    (16, 3, 7, NULL, 'Which one would you recommend for beginners?',
     '2024-11-28 17:30:00', '2024-11-28 17:30:00'),

    -- Post 8 comments
    (17, 1, 8, NULL, 'The recording technique really works! I use it too.',
     '2024-12-01 11:30:00', '2024-12-01 11:30:00'),
    (18, 2, 8, NULL, 'Where can I find conversation clubs online?',
     '2024-12-01 12:30:00', '2024-12-01 12:30:00'),

    -- Post 9 comments
    (19, 2, 9, NULL, 'I love idioms! They make conversations more interesting.',
     '2024-12-02 15:30:00', '2024-12-02 15:30:00'),
    (20, 3, 9, NULL, 'Could you share the rest of the 10 idioms?',
     '2024-12-02 16:30:00', '2024-12-02 16:30:00'),

    -- Post 10 comments
    (21, 2, 10, NULL, 'I took TOEIC last year. My advice: practice with actual test questions.',
     '2024-12-03 10:30:00', '2024-12-03 10:30:00'),
    (22, 3, 10, NULL, 'Focus on Part 5 and Part 6 for quick score improvements!',
     '2024-12-03 11:30:00', '2024-12-03 11:30:00');

-- 대댓글 샘플 데이터
INSERT INTO `comments` (`id`, `member_id`, `post_id`, `parent_id`, `content`, `created_at`, `modified_at`)
VALUES
    -- Replies to Post 1 comments
    (23, 1, 1, 1, 'Welcome! Let''s learn together!',
     '2024-11-01 11:45:00', '2024-11-01 11:45:00'),

    -- Replies to Post 2 comments
    (24, 2, 2, 4, 'For beginners, I recommend "Finding Nemo" or "Toy Story". Clear pronunciation!',
     '2024-11-05 16:45:00', '2024-11-05 16:45:00'),
    (25, 3, 2, 4, 'Friends is also great for learning everyday English!',
     '2024-11-05 17:00:00', '2024-11-05 17:00:00'),

    -- Replies to Post 3 comments
    (26, 3, 3, 6, 'I''m in Seoul, KST timezone. How about you?',
     '2024-11-10 10:15:00', '2024-11-10 10:15:00'),
    (27, 3, 3, 7, 'Great! Let''s create a group chat!',
     '2024-11-10 11:15:00', '2024-11-10 11:15:00'),

    -- Replies to Post 4 comments
    (28, 4, 4, 9, 'Sure! I''ll write a detailed post about it soon.',
     '2024-11-15 18:45:00', '2024-11-15 18:45:00'),

    -- Replies to Post 5 comments
    (29, 5, 5, 12, 'Oh yes! Rachel''s English is fantastic for pronunciation!',
     '2024-11-20 13:45:00', '2024-11-20 13:45:00'),

    -- Replies to Post 6 comments
    (30, 1, 6, 14, 'I''ll make a separate post about articles. It''s a common question!',
     '2024-11-25 15:45:00', '2024-11-25 15:45:00'),

    -- Replies to Post 7 comments
    (31, 2, 7, 16, 'I''d say start with "6 Minute English" - short and easy to understand!',
     '2024-11-28 17:45:00', '2024-11-28 17:45:00'),

    -- Replies to Post 8 comments
    (32, 6, 8, 18, 'Try MeetUp or ConversationExchange.com. They have great communities!',
     '2024-12-01 12:45:00', '2024-12-01 12:45:00'),

    -- Replies to Post 9 comments
    (33, 7, 9, 20, 'Sure! I''ll update the post with all 10 idioms tonight.',
     '2024-12-02 16:45:00', '2024-12-02 16:45:00'),

    -- Replies to Post 10 comments
    (34, 8, 10, 21, 'Thanks! Where can I find practice tests?',
     '2024-12-03 10:45:00', '2024-12-03 10:45:00'),
    (35, 2, 10, 34, 'Check out "Hackers TOEIC" series. They have excellent practice tests!',
     '2024-12-03 11:00:00', '2024-12-03 11:00:00');

