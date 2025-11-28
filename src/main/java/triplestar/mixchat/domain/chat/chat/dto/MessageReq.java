package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;

@Schema(description = "채팅 메시지 전송 요청")
public record MessageReq(
        @NotNull
        @Schema(description = "채팅방 ID", example = "1")
        Long roomId,

        @NotBlank
        @Schema(description = "메시지 내용", example = "안녕하세요!")
        String content,

        @NotNull
        @Schema(description = "메시지 타입", example = "TALK")
        ChatMessage.MessageType messageType,

        @NotNull
        @Schema(description = "대화방 타입", example = "GROUP")
        ChatRoomType chatRoomType // chatRoomType 추가
) {
}
