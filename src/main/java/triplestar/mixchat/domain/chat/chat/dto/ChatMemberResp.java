package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.member.member.entity.Member;

@Schema(description = "채팅방 멤버 정보")
public record ChatMemberResp(
        @Schema(description = "멤버 ID", example = "1")
        Long id,

        @Schema(description = "멤버 닉네임", example = "JohnDoe")
        String nickname,

        @Schema(description = "친구 여부", example = "false")
        boolean isFriend
) {
    public static ChatMemberResp from(Member member, boolean isFriend) {
        return new ChatMemberResp(member.getId(), member.getNickname(), isFriend);
    }
}
