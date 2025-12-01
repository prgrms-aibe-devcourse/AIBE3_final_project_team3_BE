package triplestar.mixchat.domain.ai.rag.context.chathistory;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.ai.rag.context.chathistory.ChatTurn.Sender;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;

@Component
@RequiredArgsConstructor
public class ChatHistoryProvider {

    private final ChatMessageRepository chatMessageRepository;

    public List<ChatTurn> getRecentHistory(Long roomId, int maxTurns) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
                roomId, ChatRoomType.AI, Pageable.ofSize(maxTurns)
        );

        return messages.stream()
                .map(msg -> new ChatTurn(
                        // TODO : ChatMessage Entity ai 발신자 구분 필요
                        Sender.USER,
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .toList();
    }
}
