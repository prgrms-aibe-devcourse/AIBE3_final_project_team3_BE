package triplestar.mixchat.domain.ai.systemprompt.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "번역 응답")
public record TranslationResp(
    @Schema(description = "원본 내용", example = "Hello", requiredMode = REQUIRED)
    @JsonProperty("original_content") String originalContent,

    @Schema(description = "번역된 내용", example = "안녕하세요", requiredMode = REQUIRED)
    @JsonProperty("corrected_content") String correctedContent,

    @Schema(description = "번역 피드백 목록", requiredMode = REQUIRED)
    @JsonProperty("feedback") List<TranslationFeedbackResp> feedback
) {
}
