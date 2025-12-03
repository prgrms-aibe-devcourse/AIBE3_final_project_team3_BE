package triplestar.mixchat.domain.ai.rag.context.chathistory;

import java.time.LocalDateTime;

public record ChatTurn(
        Sender sender,           // USER or AI
        String content,          // 실제 메시지 내용
        LocalDateTime createdAt
) {
    public enum Sender {
        USER,
        AI
    }
}