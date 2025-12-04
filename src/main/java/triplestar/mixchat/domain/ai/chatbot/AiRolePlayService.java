package triplestar.mixchat.domain.ai.chatbot;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.rag.context.chathistory.ChatHistoryProvider;
import triplestar.mixchat.domain.ai.systemprompt.constant.PromptKey;
import triplestar.mixchat.domain.ai.systemprompt.service.SystemPromptService;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class AiRolePlayService {

    private final ChatClient ollamaChatClient;
    private final SystemPromptService systemPromptService;
    private final ChatHistoryProvider chatHistoryProvider;
    private final MemberRepository memberRepository;

    // TODO : cache 적용하여 template 캐싱
    public String chat(Long userId, AIChatRoom chatRoom, String userMessage, String persona) {
        String template = systemPromptService.getLatestByKey(PromptKey.AI_ROLE_PLAY).getContent();

        String userLevel = memberRepository.findById(userId)
                .map(member -> member.getEnglishLevel().name())
                .orElse("BEGINNER");

        String chatHistory = chatHistoryProvider.getRecentHistoryString(chatRoom.getId(), 4);

        String prompt = template.replace("{{PERSONA}}", persona)
                .replace("{{USER_ENGLISH_LEVEL}}", userLevel)
                .replace("{{CHAT_HISTORY}}", chatHistory)
                .replace("{{USER_MESSAGE}}", userMessage);

        return ollamaChatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}
