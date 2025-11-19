package triplestar.mixchat.domain.miniGame.sentenceGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SentenceGameStartReq(
        @NotNull
        @Min(1)
        @Schema(description = "요청 문제 수", example = "3")
        Integer count
) {}