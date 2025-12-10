package triplestar.mixchat.domain.ai.rag.temp;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "임시 AI 응답")
public record TempAiResp(
        @Schema(description = "응답 메시지 내용", example = "안녕하세요", requiredMode = REQUIRED)
        String message
){
}
