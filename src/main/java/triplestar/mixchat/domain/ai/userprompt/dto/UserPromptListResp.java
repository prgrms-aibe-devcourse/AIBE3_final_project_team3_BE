package triplestar.mixchat.domain.ai.userprompt.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.ai.userprompt.entity.UserPrompt;

@Schema(description = "프롬프트 목록 응답 DTO")
public record UserPromptListResp(
        @Schema(description = "프롬프트 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "프롬프트 제목", example = "상황극 프롬프트", requiredMode = REQUIRED)
        String title,

        @Schema(description = "프롬프트 타입", example = "CUSTOM", requiredMode = REQUIRED)
        String promptType
) {
    public UserPromptListResp(UserPrompt userPrompt) {
        this(
            userPrompt.getId(),
            userPrompt.getTitle(),
            userPrompt.getType().name()
        );
    }
}
