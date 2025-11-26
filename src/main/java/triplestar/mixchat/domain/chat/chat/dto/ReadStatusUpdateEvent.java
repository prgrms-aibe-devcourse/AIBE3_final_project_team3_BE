package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "읽음 상태 업데이트 이벤트 (WebSocket 전용)")
public record ReadStatusUpdateEvent(
        @NotNull
        @Schema(description = "읽은 사람의 Member ID", example = "1")
        Long readerId,

        @NotNull
        @Schema(description = "읽은 메시지의 Sequence", example = "5")
        Long readSequence,

        @NotNull
        @Schema(description = "이벤트 타입", example = "READ")
        String eventType
) {
    public static ReadStatusUpdateEvent of(Long readerId, Long readSequence) {
        return new ReadStatusUpdateEvent(readerId, readSequence, "READ");
    }
}