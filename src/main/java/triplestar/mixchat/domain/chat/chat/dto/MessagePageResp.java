package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "페이징된 메시지 목록 응답")
public record MessagePageResp(
        @Schema(description = "메시지 목록")
        List<MessageResp> messages,

        @Schema(description = "다음 페이지를 로드하기 위한 커서 (마지막 메시지의 sequence)", example = "975")
        Long nextCursor,

        @Schema(description = "다음 페이지가 존재하는지 여부", example = "true")
        boolean hasMore
) {
    public static MessagePageResp of(List<MessageResp> messages, Long nextCursor, boolean hasMore) {
        return new MessagePageResp(messages, nextCursor, hasMore);
    }
}
