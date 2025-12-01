package triplestar.mixchat.domain.ai.rag.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.rag.context.chathistory.ChatHistoryProvider;
import triplestar.mixchat.domain.ai.rag.context.chathistory.ChatTurn;
import triplestar.mixchat.domain.ai.rag.context.user.ContextRetriever;
import triplestar.mixchat.domain.ai.rag.context.user.UserContextChunk;

@RequiredArgsConstructor
@Service
public class RagChatService {

    private final ContextRetriever contextRetriever;      // 학습노트 RAG
    private final ChatHistoryProvider chatHistoryProvider;
    private final RagPromptBuilder ragPromptBuilder;
    private final ChatClient chatClient;                  // Spring AI

    public String chat(Long userId, Long roomId, String userMessage) {

        // 1) 장기 기억: 유저 학습 컨텍스트
        List<UserContextChunk> chunks =
                contextRetriever.retrieve(userId, userMessage, 10);

        // 2) 단기 기억: 현재 AI와의 채팅 대화 로그
         List<ChatTurn> history = chatHistoryProvider.getRecentHistory(roomId, 10);

        // 3) 프롬프트 생성
        String prompt = ragPromptBuilder.buildPrompt(userMessage, chunks, history);

        // 4) LLM 호출
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}

