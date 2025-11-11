package triplestar.mixchat.domain.chat.chat.dto;

import jakarta.validation.constraints.NotNull;

public record CreateDirectChatReq(
        @NotNull
        Long partnerId
) {
}
