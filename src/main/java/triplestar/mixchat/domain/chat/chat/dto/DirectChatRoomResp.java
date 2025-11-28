package triplestar.mixchat.domain.chat.chat.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom;

@Schema(description = "1:1 채팅방 응답")
public record DirectChatRoomResp(
        @NotNull
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @NotNull
        @Schema(description = "첫 번째 사용자 정보", requiredMode = REQUIRED)
        ChatMemberResp user1,

        @NotNull
        @Schema(description = "두 번째 사용자 정보", requiredMode = REQUIRED)
        ChatMemberResp user2
) {
    public static DirectChatRoomResp from(DirectChatRoom entity) {
        return new DirectChatRoomResp(
                entity.getId(),
                ChatMemberResp.from(entity.getUser1(), true),
                ChatMemberResp.from(entity.getUser2(), true)
        );
    }
}