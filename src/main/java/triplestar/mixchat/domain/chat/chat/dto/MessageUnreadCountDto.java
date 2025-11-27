package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "메시지별 읽음 카운트 업데이트 정보")
public record MessageUnreadCountDto(
    @NotBlank
    @Schema(description = "메시지 고유 ID", example = "60c72b2f9b1d8e001f8e4bde", requiredMode = Schema.RequiredMode.REQUIRED)
    String messageId,

    @NotNull
    @Schema(description = "갱신된 읽지 않은 사람 수", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer unreadCount
) {
}
