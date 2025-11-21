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
        String nickname
) {
    public static ChatMemberResp from(Member member) {
        return new ChatMemberResp(member.getId(), member.getNickname());
    }
}
