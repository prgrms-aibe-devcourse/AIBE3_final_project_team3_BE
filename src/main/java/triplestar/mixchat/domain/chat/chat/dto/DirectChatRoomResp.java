package triplestar.mixchat.domain.chat.chat.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom;

@Schema(description = "1:1 채팅방 응답")
public record DirectChatRoomResp(
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "첫번째 사용자 정보", requiredMode = REQUIRED)
        ChatMemberResp user1,

        @Schema(description = "두번째 사용자 정보", requiredMode = REQUIRED)
        ChatMemberResp user2,

        @Schema(description = "안 읽은 메시지 수", example = "10", requiredMode = REQUIRED)
        Long unreadCount,

        @Schema(description = "마지막으로 읽은 Sequence", example = "50")
        Long lastReadSequence,

        @Schema(description = "마지막 메시지 시각", requiredMode = REQUIRED)
        LocalDateTime lastMessageAt,

        @Schema(description = "마지막 메시지 내용", example = "안녕하세요!")
        String lastMessageContent
) {
    public static DirectChatRoomResp from(DirectChatRoom entity, Long unreadCount, Long lastReadSequence, String lastMessageContent) {
        return new DirectChatRoomResp(
                entity.getId(),
                ChatMemberResp.from(entity.getUser1(), true),
                ChatMemberResp.from(entity.getUser2(), true),
                unreadCount,
                lastReadSequence,
                entity.getModifiedAt(),
                lastMessageContent
        );
    }

    // lastReadSequence 추가되며 기존 호환성 유지하도록 추가
    public static DirectChatRoomResp from(DirectChatRoom entity, Long unreadCount, String lastMessageContent) {
        return from(entity, unreadCount, null, lastMessageContent);
    }
}