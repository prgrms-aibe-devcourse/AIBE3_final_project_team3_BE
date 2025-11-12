package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreatePublicChatReq(
        @NotBlank
        @Schema(description = "공개 채팅방 이름", example = "자유롭게 대화해요!")
        String roomName
) {
}
