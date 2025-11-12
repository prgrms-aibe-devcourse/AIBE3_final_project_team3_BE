package triplestar.mixchat.domain.learningNote.learningNote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "학습노트 생성 요청")
public record LearningNoteCreateReq(
        @NotNull
        @Schema(description = "학습노트를 소유할 회원 ID", example = "123")
        Long memberId,

        @NotNull
        @Schema(description = "원본 텍스트", example = "I goes to school every day.")
        String originalContent,

        @NotNull
        @Schema(description = "AI가 수정한 최종 텍스트", example = "I go to school every day.")
        String correctedContent,

        @NotNull
        @Schema(description = "피드백 리스트", example = """
                        [{"tag":"Grammar","problem":"goes","correction":"go","extra":"시제 수정"},]""")
        List<FeedbackItemReq> feedback
) {}