package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "읽음 카운트 갱신 이벤트")
public record UnreadCountUpdateEvent(
    @NotNull
    @Schema(description = "읽음 카운트가 갱신된 메시지 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<MessageUnreadCountResp> updates
) {
    public static UnreadCountUpdateEvent from(List<MessageUnreadCountResp> updates) {
        return new UnreadCountUpdateEvent(updates);
    }
}
