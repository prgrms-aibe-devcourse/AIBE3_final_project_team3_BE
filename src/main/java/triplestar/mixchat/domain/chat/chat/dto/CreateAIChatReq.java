package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType;

@Schema(description = "AI 채팅방 생성 요청")
public record CreateAIChatReq(
        @NotBlank
        @Schema(description = "채팅방 이름", example = "AI 챗봇방")
        String roomName,

        @NotNull
        @Schema(description = "AI 페르소나 ID", example = "2")
        Long personaId,

        @NotNull
        @Schema(description = "채팅방 유형", example = "ROLE_PLAY")
        AiChatRoomType roomType
) {
}
