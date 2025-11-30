package triplestar.mixchat.domain.ai.rag.context;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserContextChunk {

    private final String text;      // LLM에 들어갈 실제 텍스트
    private final String type;      // "LEARNING_NOTE", "FEEDBACK" 등
    private final Long sourceId;    // 원본 레코드 ID
    private final String metadata;  // JSON 등


}