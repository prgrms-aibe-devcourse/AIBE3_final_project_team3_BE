package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import java.util.List;

@Schema(description = "채팅방 페이징 데이터 응답")
public record ChatRoomPageDataResp(
    @Schema(description = "대화방 타입", example = "DIRECT")
    ChatRoomType chatRoomType,

    @Schema(description = "메시지 목록")
    List<MessageResp> messages,

    @Schema(description = "다음 페이지를 로드하기 위한 커서 (마지막 메시지의 sequence)", example = "975")
    Long nextCursor,

    @Schema(description = "다음 페이지가 존재하는지 여부", example = "true")
    boolean hasMore
) {
    public static ChatRoomPageDataResp of(ChatRoomType chatRoomType, MessagePageResp messagePageResp) {
        return new ChatRoomPageDataResp(
            chatRoomType,
            messagePageResp.messages(),
            messagePageResp.nextCursor(),
            messagePageResp.hasMore()
        );
    }
}
