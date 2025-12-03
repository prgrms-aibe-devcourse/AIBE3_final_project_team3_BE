package triplestar.mixchat.domain.ai.systemprompt.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "번역 응답")
public record TranslationResp(
    @Schema(description = "원본 내용", example = "안녕하세요", requiredMode = REQUIRED)
    String originalContent,

    @Schema(description = "번역된 내용", example = "Hello", requiredMode = REQUIRED)
    String translatedContent
) {
}
