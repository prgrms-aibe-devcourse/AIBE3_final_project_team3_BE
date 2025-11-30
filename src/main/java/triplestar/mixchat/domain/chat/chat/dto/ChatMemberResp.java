package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.member.member.entity.Member;

@Schema(description = "채팅방 멤버 정보")
public record ChatMemberResp(
        @NotNull
        @Schema(description = "멤버 ID", example = "1")
        Long id,

        @NotNull
        @Schema(description = "멤버 닉네임", example = "JohnDoe")
        String nickname,

        @NotNull
        @Schema(description = "친구 여부", example = "false")
        boolean isFriend
) {
    public static ChatMemberResp from(Member member, boolean isFriend) {
        return new ChatMemberResp(member.getId(), member.getNickname(), isFriend);
    }
}
