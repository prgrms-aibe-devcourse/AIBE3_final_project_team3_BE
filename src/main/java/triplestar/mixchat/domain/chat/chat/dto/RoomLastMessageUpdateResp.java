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

        @Schema(description = "최근 메시지 발신 시간", example = "2025-12-04T10:30:00", requiredMode = REQUIRED)
        String lastMessageAt,

        @Schema(description = "해당 사용자의 안읽은 메시지 수", example = "3", requiredMode = REQUIRED)
        int unreadCount
) {
}
