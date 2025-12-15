package triplestar.mixchat.domain.ai.translation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "AI 피드백 요청")
public record AiFeedbackReq(
    @NotBlank
    @Schema(description = "원본 메시지 내용", example = "I goes to school")
    String originalContent,

    @NotBlank
    @Schema(description = "번역된 메시지 내용 (또는 비교 대상)", example = "나는 학교에 간다")
    String translatedContent
) {}
