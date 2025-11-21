package triplestar.mixchat.domain.chat.chat.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom; // DirectChatRoom 엔티티 사용

public record DirectChatRoomResp(
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "첫 번째 사용자 정보", requiredMode = REQUIRED)
        MemberDto user1,

        @Schema(description = "두 번째 사용자 정보", requiredMode = REQUIRED)
        MemberDto user2
) {
    public static DirectChatRoomResp from(DirectChatRoom entity) {
        return new DirectChatRoomResp(
                entity.getId(),
                MemberDto.from(entity.getUser1()),
                MemberDto.from(entity.getUser2())
        );
    }
}
