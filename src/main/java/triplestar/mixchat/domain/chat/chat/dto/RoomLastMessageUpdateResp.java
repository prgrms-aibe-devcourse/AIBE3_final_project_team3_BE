package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "채팅방 최근 메시지 업데이트 응답")
public record RoomLastMessageUpdateResp(
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long roomId,

        @Schema(description = "채팅방 타입", example = "DIRECT", requiredMode = REQUIRED)
        ChatRoomType chatRoomType,

        @Schema(description = "메시지 발신자 ID", example = "123", requiredMode = REQUIRED)
        Long senderId,

        @Schema(description = "최근 메시지 발신 시간", example = "2025-12-04T10:30:00", requiredMode = REQUIRED)
        String lastMessageAt,

        @Schema(description = "최신 메시지 Sequence (클라이언트가 unreadCount 계산에 사용)", example = "100", requiredMode = REQUIRED)
        Long latestSequence,

        @Schema(description = "최근 메시지 내용", example = "안녕하세요!")
        String lastMessageContent
) {
}
