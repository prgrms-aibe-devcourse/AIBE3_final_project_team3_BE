package triplestar.mixchat.domain.ai.translation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import triplestar.mixchat.domain.learningNote.constant.TranslationTagCode;

@Schema(description = "AI 피드백 응답")
public record AiFeedbackResp(
    @Schema(description = "AI가 교정한 원문", example = "I go to school")
    String correctedContent,

    @Schema(description = "피드백 리스트")
    List<FeedbackItem> feedback
) {
    @Schema(description = "AI 피드백 개별 항목")
    public record FeedbackItem(
        @Schema(description = "피드백 태그 (GRAMMAR, VOCABULARY 등)", example = "GRAMMAR")
        TranslationTagCode tag,

        @Schema(description = "문제가 된 부분", example = "goes")
        String problem,

        @Schema(description = "수정 제안", example = "go")
        String correction,

        @Schema(description = "상세 설명", example = "주어 I에는 3인칭 단수형이 아닌 원형 동사를 써야 합니다.")
        String extra
    ) {}
}