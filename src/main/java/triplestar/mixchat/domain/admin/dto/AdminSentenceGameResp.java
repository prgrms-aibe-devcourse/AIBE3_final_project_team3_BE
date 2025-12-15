package triplestar.mixchat.domain.admin.dto;

import java.time.LocalDateTime;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;

public record AdminSentenceGameResp(
        Long id,
        String originalContent,
        String correctedContent,
        LocalDateTime createdAt
        ) {
    public static AdminSentenceGameResp from(SentenceGame game) {
        return new AdminSentenceGameResp(
                game.getId(),
                game.getOriginalContent(),
                game.getCorrectedContent(),
                game.getCreatedAt()
        );
    }
}