package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage.chatRoomType;

@Schema(description = "채팅방 데이터 응답 (메시지 목록 포함)")
public record ChatRoomDataResp(
    @NotNull
    @Schema(description = "대화방 타입", example = "DIRECT")
    chatRoomType chatRoomType,

    @NotNull
    @Schema(description = "메시지 목록")
    List<MessageResp> messages
) {
}
