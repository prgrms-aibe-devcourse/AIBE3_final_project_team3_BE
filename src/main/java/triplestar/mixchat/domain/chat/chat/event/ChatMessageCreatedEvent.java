package triplestar.mixchat.domain.chat.chat.event;

import java.time.LocalDateTime;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

public record ChatMessageCreatedEvent(
        String messageId,
        Long roomId,
        Long senderId,
        String senderNickname,
        String content,
        ChatMessage.MessageType messageType,
        ChatRoomType chatRoomType,
        Long sequence,
        boolean translateEnabled,
        LocalDateTime createdAt,
        int unreadCount
) {
    public ChatMessageCreatedEvent {
        if (messageId == null
                || roomId == null
                || senderId == null
                || senderNickname == null
                || content == null
                || messageType == null
                || chatRoomType == null
                || sequence == null
                || createdAt == null) {
            throw new IllegalArgumentException("메시지 생성 이벤트의 필수 값이 누락되었습니다.");
        }
    }
}
