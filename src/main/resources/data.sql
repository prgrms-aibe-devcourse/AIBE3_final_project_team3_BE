-- 일반 회원 1
INSERT INTO members (
    email,
    password,
    name,
    nickname,
    country,
    english_level,
    interests,
    description,
    role,
    membership_grade,
    last_sign_in_at,
    is_blocked,
    is_deleted
) VALUES (
             'coder.dev@mixchat.com',
             '$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S', -- test1234
             '김현수',
             'SpringMaster',
             'SOUTH_KOREA',
             'INTERMEDIATE',
             '["Backend","Spring","Database"]',
             'Spring 부트 개발자입니다. 기술 토론을 좋아해요.',
             'ROLE_MEMBER',
             'BASIC',
             NOW(),
             FALSE,
             FALSE
         );

-- 일반 회원 2
INSERT INTO members (
    email,
    password,
    name,
    nickname,
    country,
    english_level,
    interests,
    description,
    role,
    membership_grade,
    last_sign_in_at,
    is_blocked,
    is_deleted
) VALUES (
             'traveler.john@mixchat.com',
             '$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
             'John Smith',
             'WorldExplorer',
             'UNITED_STATES',
             'NATIVE',
             '["Travel","Photography","History"]',
             '새로운 문화를 배우는 것을 즐깁니다.',
             'ROLE_MEMBER',
             'PREMIUM',
             NOW(),
             FALSE,
             FALSE
         );

-- 관리자 계정
INSERT INTO members (
    email,
    password,
    name,
    nickname,
    country,
    english_level,
    interests,
    description,
    role,
    membership_grade,
    last_sign_in_at,
    is_blocked,
    is_deleted
) VALUES (
             'admin.user@mixchat.com',
             '$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
             '박관리',
             'SiteManager',
             'SOUTH_KOREA',
             'ADVANCED',
             '["Management","Policy"]',
             '사이트 운영 및 관리를 담당합니다.',
             'ROLE_ADMIN',
             'BASIC',
             NOW(),
             FALSE,
             FALSE
         );

-- 차단된 회원
INSERT INTO members (
    email,
    password,
    name,
    nickname,
    country,
    english_level,
    interests,
    description,
    role,
    membership_grade,
    last_sign_in_at,
    is_blocked,
    blocked_at,
    is_deleted
) VALUES (
             'blocked.user@mixchat.com',
             '$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
             '최차단',
             'BadBehavior',
             'CHINA',
             'BEGINNER',
             '["Games"]',
             '차단된 사용자입니다.',
             'ROLE_MEMBER',
             'BASIC',
             NOW(),
             TRUE,
             NOW(),
             FALSE
         );

-- 삭제된 회원
INSERT INTO members (
    email,
    password,
    name,
    nickname,
    country,
    english_level,
    interests,
    description,
    role,
    membership_grade,
    last_sign_in_at,
    is_blocked,
    is_deleted,
    deleted_at
) VALUES (
             'deleted.user@mixchat.com',
             '$2a$10$k1PEYBKO83YurbOiVR/tc.HUEdc9w9ZTYPlYTjBznNcNiWnZ6Bl5S',
             '이삭제',
             'GhostUser',
             'JAPAN',
             'INTERMEDIATE',
             '["Anime","Movies"]',
             '삭제된 사용자입니다.',
             'ROLE_MEMBER',
             'BASIC',
             '2025-11-01 10:00:00.000000',
             FALSE,
             TRUE,
             '2025-11-01 10:00:00.000000'
         );