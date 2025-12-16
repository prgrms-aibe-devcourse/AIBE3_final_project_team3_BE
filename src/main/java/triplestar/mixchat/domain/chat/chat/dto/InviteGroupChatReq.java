package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "그룹 채팅방 멤버 초대 요청")
public record InviteGroupChatReq(
    @Schema(description = "초대할 대상 멤버 ID", example = "5")
    @NotNull
    Long targetMemberId
) {}