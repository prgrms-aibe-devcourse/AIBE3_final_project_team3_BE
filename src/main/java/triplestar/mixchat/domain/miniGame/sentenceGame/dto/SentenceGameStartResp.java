package triplestar.mixchat.domain.miniGame.sentenceGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;

public record SentenceGameStartResp(
        @Schema(description = "출제된 문제 리스트")
        List<QuestionItem> questions
) {
    public static SentenceGameStartResp from(List<SentenceGame> entities) {
        return new SentenceGameStartResp(
                entities.stream()
                        .map(QuestionItem::from)
                        .toList()
        );
    }

    public record QuestionItem(
            @Schema(description = "문제 ID", example = "10")
            Long id,

            @Schema(description = "틀린 문장", example = "I goed to school.")
            String originalContent
    ) {
        public static QuestionItem from(SentenceGame entity) {
            return new QuestionItem(
                    entity.getId(),
                    entity.getOriginalContent()
            );
        }
    }
}