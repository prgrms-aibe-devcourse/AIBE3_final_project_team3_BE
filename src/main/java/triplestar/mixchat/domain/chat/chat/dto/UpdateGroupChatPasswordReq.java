package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 채팅방 비밀번호 변경 요청")
public record UpdateGroupChatPasswordReq(
        @Schema(description = "새로운 비밀번호 (비워두면 비밀번호 제거)", example = "newpassword123")
        String newPassword
) {
}
