package triplestar.mixchat.domain.ai.chatbot;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.rag.context.chathistory.ChatHistoryProvider;
import triplestar.mixchat.domain.ai.systemprompt.constant.PromptKey;
import triplestar.mixchat.domain.ai.systemprompt.service.SystemPromptService;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;

@Service
@RequiredArgsConstructor
public class AiFreeTalkService {

    private final ChatClient ollamaChatClient;
    private final SystemPromptService systemPromptService;
    private final ChatHistoryProvider chatHistoryProvider;

    public String chat(AIChatRoom chatRoom, String userMessage, String persona) {
        String template = systemPromptService.getLatestByKey(PromptKey.AI_FREE_TALK).getContent();

        String chatHistory = chatHistoryProvider.getRecentHistoryString(chatRoom.getId(), 4);

        String replace = template.replace("{{PERSONA}}", persona)
                .replace("{{CHAT_HISTORY}}", chatHistory)
                .replace("{{USER_MESSAGE}}", userMessage);

        return ollamaChatClient
                .prompt()
                .user(replace)
                .call()
                .content();
    }
}
