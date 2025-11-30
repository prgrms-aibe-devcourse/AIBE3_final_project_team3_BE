package triplestar.mixchat.domain.ai.rag.context;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserContextChunk {

    private final Map<String,String> text;      // LLM에 들어갈 실제 텍스트를 저장하는 맵
    private final ContextChunkType type;      // "LEARNING_NOTE", "FEEDBACK" 등
    private final Long sourceId;    // 원본 ID
    private final LocalDateTime timestamp;
}