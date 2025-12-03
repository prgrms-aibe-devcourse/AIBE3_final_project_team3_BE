package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "텍스트 메시지 전송 요청")
public record TextMessageReq(
        @NotBlank
        @Schema(description = "메시지 내용", example = "안녕하세요!")
        String content,

        @Schema(description = "자동 번역 여부", example = "true")
        boolean isTranslateEnabled
) {
}
