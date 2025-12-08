package triplestar.mixchat.domain.ai.rag.context.user;

import java.util.List;

public interface ContextRetriever {
    List<UserContextChunk> retrieve(Long roomId, Long userId, String userMessage, int maxItems);
}
