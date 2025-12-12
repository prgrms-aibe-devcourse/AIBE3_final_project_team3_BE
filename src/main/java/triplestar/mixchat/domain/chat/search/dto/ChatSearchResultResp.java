package triplestar.mixchat.domain.chat.search.dto;

import java.time.LocalDateTime;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.search.document.ChatMessageDocument;

public record ChatSearchResultResp(
        String messageId,
        Long chatRoomId,
        ChatRoomType chatRoomType,
        String senderName,
        String content,
        String translatedContent,
        Long sequence,
        LocalDateTime createdAt
) {
    public static ChatSearchResultResp fromDocument(ChatMessageDocument document) {
        return new ChatSearchResultResp(
                document.getMessageId(),
                document.getChatRoomId(),
                document.getChatRoomType(),
                document.getSenderName(),
                document.getContent(),
                document.getTranslatedContent(),
                document.getSequence(),
                document.getCreatedAt()
        );
    }
}
