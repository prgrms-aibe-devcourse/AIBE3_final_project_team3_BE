ALTER TABLE ai_chat_rooms
    ADD COLUMN current_sequence BIGINT NOT NULL DEFAULT 0;