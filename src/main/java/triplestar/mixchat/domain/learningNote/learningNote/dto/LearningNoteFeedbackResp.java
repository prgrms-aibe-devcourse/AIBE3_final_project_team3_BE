package triplestar.mixchat.domain.learningNote.learningNote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;

@Schema(description = "학습노트 목록 조회 응답 데이터")
public record LearningNoteFeedbackResp(

        @Schema(description = "학습노트 정보",requiredMode = Schema.RequiredMode.REQUIRED)
        LearningNoteResp note,

        @Schema(description = "피드백 정보",requiredMode = Schema.RequiredMode.REQUIRED)
        FeedbackResp feedback
) {
    public static LearningNoteFeedbackResp create(LearningNote note, Feedback fb) {
        return new LearningNoteFeedbackResp(
                new LearningNoteResp(
                        note.getId(),
                        note.getOriginalContent(),
                        note.getCorrectedContent()
                ),
                FeedbackResp.from(fb)
        );
    }
}
