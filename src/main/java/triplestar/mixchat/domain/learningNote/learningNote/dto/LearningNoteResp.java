package triplestar.mixchat.domain.learningNote.learningNote.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "학습노트 목록 응답 데이터")
public record LearningNoteResp(
        @Schema(description = "원본 문장", example = "I goes to school yesterday.", requiredMode = REQUIRED)
        String originalContent,

        @Schema(description = "AI가 수정한 최종 문장", example = "I went to school yesterday.", requiredMode = REQUIRED)
        String correctedContent,

        @Schema(description = "피드백 목록", requiredMode = REQUIRED)
        List<FeedbackListResp> feedback
) {}
