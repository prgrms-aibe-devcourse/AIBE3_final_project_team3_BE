package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "채팅방 참가 응답")
public record JoinRoomResp(
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long id
) {
    public static JoinRoomResp of(Long id) {
        return new JoinRoomResp(id);
    }
}
