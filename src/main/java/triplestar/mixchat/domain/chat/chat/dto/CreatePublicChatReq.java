package triplestar.mixchat.domain.chat.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePublicChatReq(
        @NotBlank
        String roomName
) {
}
