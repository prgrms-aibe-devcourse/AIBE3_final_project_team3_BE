package triplestar.mixchat.domain.member.friend.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.member.friend.entity.FriendshipRequest;

@Schema(description = "친구 요청 생성 후 응답 DTO")
public record FriendshipRequestResp(
        @Schema(description = "생성된 친구 요청의 고유 ID", example = "42", requiredMode = REQUIRED)
        Long id
) {
    public static FriendshipRequestResp from(FriendshipRequest entity) {
        return new FriendshipRequestResp(
                entity.getId()
        );
    }
}
