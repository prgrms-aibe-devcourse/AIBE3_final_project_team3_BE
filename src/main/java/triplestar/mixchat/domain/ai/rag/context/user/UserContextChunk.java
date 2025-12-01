package triplestar.mixchat.domain.ai.rag.context.user;

import java.time.LocalDateTime;
import java.util.Map;

public record UserContextChunk(
        // LLM에 들어갈 실제 텍스트를 저장하는 맵
        Map<String, String> text,
        // "LEARNING_NOTE", "FEEDBACK" 등
        ContextChunkType type,
        // 원본 ID
        Long sourceId,
        LocalDateTime createdAt) {
}