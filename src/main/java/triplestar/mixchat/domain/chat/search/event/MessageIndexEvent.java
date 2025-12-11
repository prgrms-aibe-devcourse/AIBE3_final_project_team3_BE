package triplestar.mixchat.domain.chat.search.event;

import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;

import java.time.LocalDateTime;

public record MessageIndexEvent(
    String messageId,
    Long chatRoomId,
    ChatRoomType chatRoomType,
    Long senderId,
    String senderNickname,
    String content,
    Long sequence,
    LocalDateTime createdAt
) {
    public MessageIndexEvent {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId는 null이거나 비어있을 수 없습니다.");
        }
        if (chatRoomId == null) {
            throw new IllegalArgumentException("chatRoomId는 null일 수 없습니다.");
        }
        if (chatRoomType == null) {
            throw new IllegalArgumentException("chatRoomType은 null일 수 없습니다.");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("senderId는 null일 수 없습니다.");
        }
        if (content == null) {
            throw new IllegalArgumentException("content는 null일 수 없습니다.");
        }
        if (sequence == null) {
            throw new IllegalArgumentException("sequence는 null일 수 없습니다.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt은 null일 수 없습니다.");
        }
    }
}
