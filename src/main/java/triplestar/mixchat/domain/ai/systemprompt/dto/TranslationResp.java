package triplestar.mixchat.domain.ai.systemprompt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TranslationResp(
    @JsonProperty("original_content") String originalContent,
    @JsonProperty("corrected_content") String correctedContent,
    @JsonProperty("feedback") List<TranslationFeedbackDto> feedback
) {
}
