package triplestar.mixchat.domain.miniGame.sentenceGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SentenceGameCountResp(
        @Schema(description = "전체 문제 수", example = "42")
        long totalCount
) {
    public static SentenceGameCountResp from(long count) {
        return new SentenceGameCountResp(count);
    }
}