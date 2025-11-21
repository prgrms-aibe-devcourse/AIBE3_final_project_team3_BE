package triplestar.mixchat.domain.chat.chat.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom; // AIChatRoom 엔티티 사용

@Schema(description = "AI 채팅방 생성 응답")
public record AIChatRoomResp(
        @NotNull
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @NotBlank
        @Schema(description = "채팅방 이름", example = "AI 챗봇방", requiredMode = REQUIRED)
        String name,

        @NotBlank
        @Schema(description = "AI 모델 ID", example = "gpt-4", requiredMode = REQUIRED)
        String aiModelId,

        @NotBlank
        @Schema(description = "AI 페르소나", example = "친절한 비서", requiredMode = REQUIRED)
        String aiPersona
) {
    public static AIChatRoomResp from(AIChatRoom entity) {
        return new AIChatRoomResp(
                entity.getId(),
                entity.getName(),
                entity.getAiModelId(),
                entity.getAiPersona()
        );
    }
}
