package triplestar.mixchat.domain.chat.chat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateGroupChatReq(
        @NotNull
        String roomName,

        @NotEmpty
        List<Long> memberIds
) {
}
