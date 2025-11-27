package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage.chatRoomType;

@Schema(description = "채팅방 페이징 데이터 응답")
public record ChatRoomPageDataResp(
    @NotNull
    @Schema(description = "대화방 타입", example = "DIRECT")
    chatRoomType chatRoomType,

    @NotNull
    @Schema(description = "페이징된 메시지 목록")
    MessagePageResp messagePageResp
) {
    public static ChatRoomPageDataResp of(chatRoomType chatRoomType, MessagePageResp messagePageResp) {
        return new ChatRoomPageDataResp(chatRoomType, messagePageResp);
    }
}
