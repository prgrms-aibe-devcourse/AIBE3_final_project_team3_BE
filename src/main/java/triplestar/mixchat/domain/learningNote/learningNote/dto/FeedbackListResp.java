package triplestar.mixchat.domain.learningNote.learningNote.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;

@Schema(description = "학습노트 내 피드백 정보")
public record FeedbackListResp(
        @Schema(description = "피드백 태그", example = "Grammar", requiredMode = REQUIRED)
        TranslationTagCode tag,

        @Schema(description = "문제 구절", example = "goes", requiredMode = REQUIRED)
        String problem,

        @Schema(description = "수정 구절", example = "went", requiredMode = REQUIRED)
        String correction,

        @Schema(description = "추가 설명", example = "시제 수정 필요", requiredMode = REQUIRED)
        String extra,

        @Schema(description = "해당 피드백 학습 완료 여부", example = "false")
        boolean marked
) {
    public static FeedbackListResp from(Feedback feedback) {
        return new FeedbackListResp(
                feedback.getTag(),
                feedback.getProblem(),
                feedback.getCorrection(),
                feedback.getExtra(),
                feedback.isMarked()
        );
    }
}
