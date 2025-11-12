package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

public record MessageReq(
        @NotNull
        @Schema(description = "채팅방 ID", example = "1")
        Long roomId,

        @NotNull
        @Schema(description = "메시지 내용", example = "안녕하세요!")
        String content,

        @NotNull
        @Schema(description = "메시지 타입", example = "TALK")
        ChatMessage.MessageType messageType
) {
}
