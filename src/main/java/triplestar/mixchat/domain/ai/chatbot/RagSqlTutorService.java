package triplestar.mixchat.domain.ai.chatbot;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.rag.context.chathistory.ChatHistoryProvider;
import triplestar.mixchat.domain.ai.rag.context.user.SqlContextRetriever;
import triplestar.mixchat.domain.ai.rag.context.user.UserContextChunk;
import triplestar.mixchat.domain.ai.rag.promptbuilder.RagPromptBuilder;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;
import triplestar.mixchat.global.ai.ChatClientChainExecutor;
import triplestar.mixchat.global.ai.ChatClientName;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagSqlTutorService {

    private final SqlContextRetriever sqlContextRetriever;      // 컨텍스트 검색기(Sql, 벡터 DB 등)
    private final ChatHistoryProvider chatHistoryProvider;      // 대화 로그 제공자
    private final RagPromptBuilder ragPromptBuilder;            // RAG 프롬프트 빌더
    private final ChatClientChainExecutor chatClientChainExecutor;

    public String chat(Long userId, AIChatRoom chatRoom, String userMessage, String persona) {
        // 1) 장기 기억: 유저 학습 컨텍스트
        List<UserContextChunk> chunks = sqlContextRetriever.retrieve(userId, 10);
        List<Message> assistantMessages = ragPromptBuilder.convertChunksToMessages(chunks);

        // 2) 단기 기억: 현재 AI와의 채팅 대화 로그
        List<Message> recentHistory = chatHistoryProvider.getRecentHistory(chatRoom.getId(), 10);

        //3) 프롬프트 생성
        String system = ragPromptBuilder.buildPrompt(persona, chatRoom.getRoomType());

        // 4) LLM 호출
        return chatClientChainExecutor.call(ChatClientName.OPENAI_ADVANCED, system, userMessage, assistantMessages);
    }
}

