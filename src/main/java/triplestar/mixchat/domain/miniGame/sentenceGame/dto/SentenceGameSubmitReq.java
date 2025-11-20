package triplestar.mixchat.domain.miniGame.sentenceGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사용자 정답 제출")
public record SentenceGameSubmitReq(
        @NotNull
        @Schema(description = "문장 ID", example = "10")
        Long sentenceGameId,

        @NotBlank
        @Schema(description = "사용자가 제출한 문장", example = "I went to school.")
        String userAnswer
) {}