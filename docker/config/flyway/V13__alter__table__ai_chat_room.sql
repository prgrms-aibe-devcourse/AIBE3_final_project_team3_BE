ALTER TABLE ai_chat_rooms
DROP COLUMN ai_model_id,
    DROP COLUMN ai_persona,
    ADD COLUMN persona_id BIGINT NOT NULL AFTER name,
    ADD COLUMN current_sequence BIGINT NOT NULL DEFAULT 0 AFTER persona_id;

INSERT INTO members (
    email, password, name, nickname, country,
    interests, english_level, description, role,
    membership_grade, last_sign_in_at, is_blocked, blocked_at,
    is_deleted, deleted_at, block_reason, profile_image_url,
    created_at, modified_at
) VALUES
-- 101
('aichatbot@bot.com','botpassword',
    'chatbot','chatbot','KR',
    '["🎮 sandbox", "🍗 chicken"]',
    'ADVANCED','chatbot','ROLE_BOT','PREMIUM',
    NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW());

