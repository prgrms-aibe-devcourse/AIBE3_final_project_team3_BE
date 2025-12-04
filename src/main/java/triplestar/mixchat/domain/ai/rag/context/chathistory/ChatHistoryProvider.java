package triplestar.mixchat.domain.ai.rag.context.chathistory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.MediaContent;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.global.ai.BotConstant;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatHistoryProvider {

    private final ChatMessageRepository chatMessageRepository;

    public List<Message> getRecentHistory(Long roomId, int maxTurns) {
        List<ChatMessage> messages = chatMessageRepository.
                findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
                roomId, ChatRoomType.AI, Pageable.ofSize(maxTurns)
        );

        return messages.stream()
                // 타입 추론 이슈로 명시적 Message 캐스팅
                .<Message>map(turn ->
                        turn.getSenderId().equals(BotConstant.BOT_MEMBER_ID)
                                ? new AssistantMessage(turn.getContent())
                                : new UserMessage(turn.getContent())
                )
                .toList();
    }
}
