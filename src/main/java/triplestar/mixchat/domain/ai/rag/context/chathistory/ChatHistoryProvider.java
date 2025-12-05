package triplestar.mixchat.domain.ai.rag.context.chathistory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.global.ai.BotConstant;

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatHistoryProvider {

    private final ChatMessageRepository chatMessageRepository;

    public List<Message> getRecentHistory(Long roomId, int maxTurns) {
        List<ChatMessage> messages = chatMessageRepository.
                findByChatRoomIdAndChatRoomTypeOrderByCreatedAtDescIdDesc(
                        roomId, ChatRoomType.AI, Pageable.ofSize(maxTurns)
                );

        List<Message> history = messages.stream()
                // 타입 추론 이슈로 명시적 Message 캐스팅
                .<Message>map(turn ->
                        turn.getSenderId().equals(BotConstant.BOT_MEMBER_ID)
                                ? new AssistantMessage(turn.getContent())
                                : new UserMessage(turn.getContent())
                )
                .collect(Collectors.toCollection(ArrayList::new));
        ;

        if (history == null || history.isEmpty()) {
            return List.of(new AssistantMessage("이전 대화가 없습니다"));
        }

        // 대화 순서대로 정렬
        Collections.reverse(history);
        return history;
    }
}
