package triplestar.mixchat.domain.miniGame.sentenceGame.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "미니게임 전체 문제 수 정보")
public record SentenceGameCountResp(
        @Schema(description = "전체 문제 수", example = "42", requiredMode = REQUIRED)
        long totalCount
) {
    public static SentenceGameCountResp from(long count) {
        return new SentenceGameCountResp(count);
    }
}