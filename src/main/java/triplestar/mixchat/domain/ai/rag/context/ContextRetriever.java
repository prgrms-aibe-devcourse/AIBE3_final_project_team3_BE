package triplestar.mixchat.domain.ai.rag.context;

import java.util.List;

public interface ContextRetriever {

    List<UserContextChunk> retrieve(Long userId, String userMessage, int maxItems);
}
