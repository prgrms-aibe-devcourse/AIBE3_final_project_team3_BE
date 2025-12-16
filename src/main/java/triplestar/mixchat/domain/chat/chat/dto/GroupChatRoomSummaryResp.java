package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "그룹 채팅방 요약 (리스트 조회용 - 최적화)")
public record GroupChatRoomSummaryResp(
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "채팅방 이름", example = "영어 스터디", requiredMode = REQUIRED)
        String name,

        @Schema(description = "방 주제", example = "언어 교환")
        String topic,

        @Schema(description = "안 읽은 메시지 수", example = "3", requiredMode = REQUIRED)
        Long unreadCount,

        @Schema(description = "마지막으로 읽은 Sequence", example = "50")
        Long lastReadSequence,

        @Schema(description = "마지막 메시지 시각")
        LocalDateTime lastMessageAt,

        @Schema(description = "마지막 메시지 내용", example = "안녕하세요!")
        String lastMessageContent
) {
    public static GroupChatRoomSummaryResp from(
            GroupChatRoom room,
            Long unreadCount,
            Long lastReadSequence,
            LocalDateTime lastMessageAt,
            String lastMessageContent
    ) {
        return new GroupChatRoomSummaryResp(
                room.getId(),
                room.getName(),
                room.getTopic(),
                unreadCount,
                lastReadSequence,
                lastMessageAt,
                lastMessageContent
        );
    }

    // 기존 호환성을 위한 메서드 (deprecated)
    public static GroupChatRoomSummaryResp from(
            GroupChatRoom room,
            Long unreadCount,
            Long lastReadSequence,
            String lastMessageContent
    ) {
        return from(room, unreadCount, lastReadSequence, room.getModifiedAt(), lastMessageContent);
    }
}
