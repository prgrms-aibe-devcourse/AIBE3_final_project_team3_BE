package triplestar.mixchat.domain.ai.systemprompt.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "번역 요청")
public record TranslationReq(
    @NotBlank
    @Schema(description = "채팅 메시지 ID", example = "60c72b2f9b1d8e001f8e4bde", requiredMode = REQUIRED)
    String chatMessageId,

    @NotBlank
    @Schema(description = "원본 메시지 내용", example = "Hello", requiredMode = REQUIRED)
    String originalContent
) {
}
