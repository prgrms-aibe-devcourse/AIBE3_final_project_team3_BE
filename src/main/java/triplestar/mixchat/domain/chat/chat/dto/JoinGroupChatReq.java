package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;

@Schema(description = "그룹 채팅방 참가 요청")
public record JoinGroupChatReq(
        @Schema(description = "채팅방 비밀번호 (비밀번호가 있는 방만)", example = "mysecretpass")
        @Nullable
        String password
) {
}
