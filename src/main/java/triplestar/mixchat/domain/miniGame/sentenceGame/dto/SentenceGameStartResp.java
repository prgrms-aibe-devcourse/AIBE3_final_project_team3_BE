package triplestar.mixchat.domain.miniGame.sentenceGame.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;

@Schema(description = "사용자에게 제공된 문제 정보")
public record SentenceGameStartResp(
        @Schema(description = "출제된 문제 리스트", requiredMode = REQUIRED)
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
            @Schema(description = "문제 ID", example = "10", requiredMode = REQUIRED)
            Long id,

            @Schema(description = "틀린 문장", example = "I goed to school.", requiredMode = REQUIRED)
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