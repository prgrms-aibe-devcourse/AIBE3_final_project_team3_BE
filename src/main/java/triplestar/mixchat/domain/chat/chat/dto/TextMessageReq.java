package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "텍스트 메시지 전송 요청 (WebSocket)")
public record TextMessageReq(
        @NotBlank
        @Schema(description = "메시지 내용", example = "안녕하세요!")
        String content
) {
}
