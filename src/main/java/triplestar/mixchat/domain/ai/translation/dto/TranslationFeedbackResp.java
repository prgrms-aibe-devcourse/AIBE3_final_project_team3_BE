package triplestar.mixchat.domain.ai.translation.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "번역 피드백 응답 DTO")
public record TranslationFeedbackResp(
    @Schema(description = "피드백 태그", example = "오역", requiredMode = REQUIRED)
    String tag,

    @Schema(description = "문제점", example = "번역 내용이 원문과 다릅니다.", requiredMode = REQUIRED)
    String problem,

    @Schema(description = "수정 제안", example = "Hello -> 안녕하세요", requiredMode = REQUIRED)
    String correction,

    @Schema(description = "추가 정보", example = "문맥상 이 표현이 더 자연스러워요.")
    String extra
) {
}
