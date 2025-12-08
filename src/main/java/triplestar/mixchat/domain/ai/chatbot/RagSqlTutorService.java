package triplestar.mixchat.domain.ai.chatbot;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.rag.RagPromptBuilder;
import triplestar.mixchat.domain.ai.rag.context.chathistory.ChatHistoryProvider;
import triplestar.mixchat.domain.ai.rag.context.user.ContextRetriever;
import triplestar.mixchat.domain.ai.rag.context.user.UserContextChunk;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;

@Service
@RequiredArgsConstructor
public class RagSqlTutorService {

    private final ContextRetriever contextRetriever;            // 컨텍스트 검색기(Sql, 벡터 DB 등)
    private final ChatHistoryProvider chatHistoryProvider;      // 대화 로그 제공자
    private final RagPromptBuilder ragPromptBuilder;            // RAG 프롬프트 빌더
    private final ChatClient ollamaChatClient;                  // LLM 채팅 클라이언트 -> 답변 품질을 보니 openAi 사용해야할 듯

    public String chat(Long userId, AIChatRoom chatRoom, String userMessage, String persona) {

        // 1) 장기 기억: 유저 학습 컨텍스트
        List<UserContextChunk> chunks = contextRetriever.retrieve(chatRoom.getId(), userId, null, 10);

        // 2) 단기 기억: 현재 AI와의 채팅 대화 로그
        List<Message> recentHistory = chatHistoryProvider.getRecentHistory(chatRoom.getId(), 10);

        //3) 프롬프트 생성
        String prompt = ragPromptBuilder.buildPrompt(chunks, persona);

        // 4) LLM 호출
        return ollamaChatClient
                .prompt()
                .system(prompt)
                .messages(recentHistory)
                .user(userMessage)
                .call()
                .content();
    }
}

