package triplestar.mixchat.domain.ai.userprompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "프롬프트 생성/수정 요청 DTO")
public record UserPromptReq(
    @NotNull
    @Schema(description = "프롬프트 제목", example = "상황극 프롬프트")
    String title,

    @NotNull
    @Schema(description = "프롬프트 내용", example = "프롬프트 작성 예시 ...")
    String content,

    @NotNull
    @Schema(description = "프롬프트 타입", example = "CUSTOM")
    String promptType
) {}

