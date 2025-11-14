package triplestar.mixchat.domain.prompt.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "프롬프트 목록 응답 DTO")
public record PromptListResp(
    @NotNull
    @Schema(description = "프롬프트 ID", example = "1")
    Long id,

    @NotNull
    @Schema(description = "프롬프트 제목", example = "상황극 프롬프트")
    String title,

    @NotNull
    @Schema(description = "프롬프트 타입", example = "CUSTOM")
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
