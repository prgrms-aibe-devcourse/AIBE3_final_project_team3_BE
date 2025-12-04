package triplestar.mixchat.domain.ai.chatbot;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.ai.rag.context.chathistory.ChatHistoryProvider;
import triplestar.mixchat.domain.ai.systemprompt.constant.PromptKey;
import triplestar.mixchat.domain.ai.systemprompt.service.SystemPromptService;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AiRolePlayService {

    private final ChatClient ollamaChatClient;
    private final SystemPromptService systemPromptService;
    private final ChatHistoryProvider chatHistoryProvider;
    private final MemberRepository memberRepository;

    public String chat(Long userId, AIChatRoom chatRoom, String userMessage, String persona) {
        String template = systemPromptService.getLatestByKey(PromptKey.AI_ROLE_PLAY).getContent();

        String userLevel = memberRepository.findById(userId)
                .map(member -> member.getEnglishLevel().name())
                .orElse("BEGINNER");

        List<Message> recentHistory = chatHistoryProvider.getRecentHistory(chatRoom.getId(), 4);

        String prompt = template.replace("{{PERSONA}}", persona)
                .replace("{{USER_ENGLISH_LEVEL}}", userLevel);

        return ollamaChatClient
                .prompt()
                .system(prompt)
                .messages(recentHistory)
                .user(userMessage)
                .call()
                .content();
    }
}
