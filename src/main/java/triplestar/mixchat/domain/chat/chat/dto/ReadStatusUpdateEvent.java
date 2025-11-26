package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "읽음 상태 업데이트 이벤트 (WebSocket 전용)")
public record ReadStatusUpdateEvent(
        @NotNull
        @Schema(description = "읽은 사람의 Member ID", example = "1")
        Long readerId,

        @NotNull
        @Schema(description = "읽은 사람 수 계산을 위해 유저가 마지막으로 읽은 LastReadSequence와 지속적으로 증가하는 Sequence를 두어 비교하는 데에 사용", example = "5")
        Long readSequence
) {
    public static ReadStatusUpdateEvent of(Long readerId, Long readSequence) {
        return new ReadStatusUpdateEvent(readerId, readSequence);
    }
}