package triplestar.mixchat.domain.ai.rag.context.user;

import java.time.LocalDateTime;
import java.util.Map;

public record UserContextChunk(
        Map<String, String> text,                // LLM에 들어갈 실제 텍스트를 저장하는 맵
        ContextChunkType type,                   // "LEARNING_NOTE", "FEEDBACK" 등
        Long sourceId,                           // 원본 데이터베이스의 ID (예: LearningNote ID)
        LocalDateTime createdAt
) {
}