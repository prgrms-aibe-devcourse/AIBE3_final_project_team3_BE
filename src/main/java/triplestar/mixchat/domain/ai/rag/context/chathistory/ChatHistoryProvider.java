package triplestar.mixchat.domain.ai.rag.context.chathistory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.ai.rag.context.chathistory.ChatTurn.Sender;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.global.ai.BotConstant;

@Component
@RequiredArgsConstructor
public class ChatHistoryProvider {

    private final ChatMessageRepository chatMessageRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<ChatTurn> getRecentHistory(Long roomId, int maxTurns) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
                roomId, ChatRoomType.AI, Pageable.ofSize(maxTurns)
        );

        return messages.stream()
                .map(msg -> new ChatTurn(
                        msg.getSenderId().equals(BotConstant.BOT_MEMBER_ID) ? Sender.AI : Sender.USER,
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .toList();
    }

    public String getRecentHistoryString(Long roomId, int maxTurns) {
        List<ChatTurn> chatTurns = getRecentHistory(roomId, maxTurns);

        return buildChatHistoryBlock(chatTurns);
    }

    private String buildChatHistoryBlock(List<ChatTurn> history) {
        if (history == null || history.isEmpty()) {
            return "(이전 대화가 없습니다. 첫 대화라고 생각하고 자연스럽게 시작해 주세요.)";
        }

        return history.stream()
                .map(this::formatChatTurn)
                .collect(Collectors.joining("\n"));
    }

    private String formatChatTurn(ChatTurn turn) {
        String roleLabel = switch (turn.sender()) {
            case USER -> "User";
            case AI -> "AiTutor";
        };

        String timeStr = DATE_FORMATTER.format(turn.createdAt());
        return "[" + timeStr + "] " + roleLabel + " : " + turn.content();
    }
}
