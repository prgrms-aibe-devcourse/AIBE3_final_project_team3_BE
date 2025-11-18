package triplestar.mixchat.domain.admin.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;

public record AdminSentenceGameListResp(

        @Schema(description = "미니게임 ID", example = "10")
        Long id,

        @Schema(description = "수정 전 문장", example = "I goed to the store yesterday.")
        String originalContent,

        @Schema(description = "수정 후 문장", example = "I went to the store yesterday.")
        String correctedContent
) {
    public static AdminSentenceGameListResp from(SentenceGame game) {
        return new AdminSentenceGameListResp(
                game.getId(),
                game.getOriginalContent(),
                game.getCorrectedContent()
        );
    }
}