package triplestar.mixchat.domain.ai.userprompt.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import triplestar.mixchat.domain.ai.userprompt.entity.Prompt;

@Schema(description = "프롬프트 상세 응답 DTO")
public record PromptDetailResp(
        @Schema(description = "프롬프트 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "프롬프트 제목", example = "상황극 프롬프트", requiredMode = REQUIRED)
        String title,

        @Schema(description = "프롬프트 타입", example = "CUSTOM", requiredMode = REQUIRED)
        String promptType,

        @Schema(description = "프롬프트 내용", example = "상황극에서 사용할 프롬프트 내용", requiredMode = REQUIRED)
        String content,

        @Schema(description = "생성일시", example = "2025-11-12T12:00:00", requiredMode = REQUIRED)
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2025-11-12T12:10:00", requiredMode = REQUIRED)
        LocalDateTime modifiedAt,

        @Schema(description = "멤버 ID", example = "1", requiredMode = REQUIRED)
        Long memberId
) {
    public PromptDetailResp(Prompt prompt) {
        this(
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
