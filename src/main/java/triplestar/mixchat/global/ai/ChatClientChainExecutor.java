package triplestar.mixchat.global.ai;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatClientChainExecutor {

    private final List<ChatClient> orderedExecutors; // @Order 순서
    private final Map<String, ChatClient> executorMap; // 명시적 선택

    public String call(ChatClientName primary, String system, String user, List<Message> context) {
        // 1) primary 선택 후 우선 시도
        ChatClient primaryClient = executorMap.get(primary.beanName());
        String content = bindResult(system, user, context, primaryClient);

        if (content != null && !content.isBlank()) {
            return content;
        }

        // 2) fallback은 @Order 순서대로
        for (ChatClient chatClient : orderedExecutors) {
            if (!chatClient.equals(primaryClient)) {
                content = bindResult(system, user, context, chatClient);
                if (content != null && !content.isBlank()) {
                    return content;
                }
            }
        }
        throw new RuntimeException("모든 provider 실패");
    }

    private String bindResult(String system, String user, List<Message> context, ChatClient chatClient) {
        try {
            return chatClient.prompt()
                    .system(system)
                    .user(user)
                    .messages(context)
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("Provider {} 실패", chatClient.getClass().getSimpleName(), e);
            return null;
        }
    }

    public String call(ChatClientName primary, String system, String user) {
        return call(primary, system, user, List.of());
    }
}
