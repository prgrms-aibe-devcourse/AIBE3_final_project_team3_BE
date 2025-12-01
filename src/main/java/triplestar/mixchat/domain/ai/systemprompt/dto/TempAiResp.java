package triplestar.mixchat.domain.ai.systemprompt.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "임시 AI 응답")
public record TempAiResp(
        @NotBlank
        @Schema(description = "응답 메시지 내용", example = "안녕하세요", requiredMode = REQUIRED)
        String message
){
}
