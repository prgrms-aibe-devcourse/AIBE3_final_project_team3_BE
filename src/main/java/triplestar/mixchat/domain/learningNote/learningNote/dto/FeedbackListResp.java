package triplestar.mixchat.domain.learningNote.learningNote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;

@Schema(description = "학습노트 내 피드백 정보")
public record FeedbackListResp(
        @NotNull
        @Schema(description = "피드백 태그", example = "Grammar")
        TranslationTagCode tag,

        @NotNull
        @Schema(description = "문제 구절", example = "goes")
        String problem,

        @NotNull
        @Schema(description = "수정 구절", example = "went")
        String correction,

        @NotNull
        @Schema(description = "추가 설명", example = "시제 수정 필요")
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
