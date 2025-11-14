package triplestar.mixchat.domain.member.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "친구 요청 생성 요청 DTO")
public record FriendshipSendReq(
        @Schema(description = "친구 요청을 받을 대상 회원의 고유 ID", example = "5")
        @NotNull(message = "수신자 ID는 필수 값입니다.")
        Long receiverId
) {
}