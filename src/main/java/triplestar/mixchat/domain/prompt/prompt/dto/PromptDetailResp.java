package triplestar.mixchat.domain.prompt.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import triplestar.mixchat.domain.prompt.prompt.entity.Prompt;

@Schema(description = "프롬프트 상세 응답 DTO")
public record PromptDetailResp(
    @NotNull
    @Schema(description = "프롬프트 ID", example = "1")
    Long id,

    @NotNull
    @Schema(description = "프롬프트 제목", example = "상황극 프롬프트")
    String title,

    @NotNull
    @Schema(description = "프롬프트 타입", example = "CUSTOM")
    String promptType,

    @NotNull
    @Schema(description = "프롬프트 내용", example = "상황극에서 사용할 프롬프트 내용")
    String content,

    @NotNull
    @Schema(description = "생성일시", example = "2025-11-12T12:00:00")
    LocalDateTime createdAt,

    @NotNull
    @Schema(description = "수정일시", example = "2025-11-12T12:10:00")
    LocalDateTime modifiedAt,

    @NotNull
    @Schema(description = "멤버 ID", example = "1")
    Long memberId
) {
    public static PromptDetailResp of(Prompt prompt) {
        return new PromptDetailResp(
            prompt.getId(),
            prompt.getTitle(),
            prompt.getType().name(),
            prompt.getContent(),
            prompt.getCreatedAt(),
            prompt.getModifiedAt(),
            prompt.getMember().getId()
        );
    }
}
