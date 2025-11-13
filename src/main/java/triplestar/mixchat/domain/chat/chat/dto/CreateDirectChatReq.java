package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "1:1 채팅방 생성 요청")
public record CreateDirectChatReq(
        @NotNull
        @Schema(description = "1:1 채팅 상대방 ID", example = "2")
        Long partnerId
) {
}
