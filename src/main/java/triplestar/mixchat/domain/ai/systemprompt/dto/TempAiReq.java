package triplestar.mixchat.domain.ai.systemprompt.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "임시 AI 요청")
public record TempAiReq(
        @NotBlank
        @Schema(description = "메시지 내용", example = "안녕", requiredMode = REQUIRED)
        String message
){
}
