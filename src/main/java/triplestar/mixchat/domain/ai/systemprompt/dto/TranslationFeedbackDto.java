package triplestar.mixchat.domain.ai.systemprompt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TranslationFeedbackDto(
    String tag,
    String problem,
    String correction,
    String extra
) {
}
