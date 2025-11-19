package triplestar.mixchat.domain.learningNote.learningNote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "학습노트 목록 응답 데이터")
public record LearningNoteResp(
        @NotNull
        @Schema(description = "원본 문장", example = "I goes to school yesterday.")
        String originalContent,

        @NotNull
        @Schema(description = "AI가 수정한 최종 문장", example = "I went to school yesterday.")
        String correctedContent,

        @NotNull
        @Schema(description = "피드백 목록")
        List<FeedbackListResp> feedback
) {}
