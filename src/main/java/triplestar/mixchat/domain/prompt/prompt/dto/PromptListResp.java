package triplestar.mixchat.domain.prompt.prompt.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "프롬프트 목록 응답 DTO")
public record PromptListResp(
        @Schema(description = "프롬프트 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "프롬프트 제목", example = "상황극 프롬프트", requiredMode = REQUIRED)
        String title,

        @Schema(description = "프롬프트 타입", example = "CUSTOM", requiredMode = REQUIRED)
        String promptType
) {
    public PromptListResp(triplestar.mixchat.domain.prompt.prompt.entity.Prompt prompt) {
        this(
            prompt.getId(),
            prompt.getTitle(),
            prompt.getType().name()
        );
    }
}
