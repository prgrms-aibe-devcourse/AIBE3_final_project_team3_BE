package triplestar.mixchat.domain.learningNote.learningNote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "피드백 학습 상태 변경 요청")
public record FeedbackMarkUpdateReq(

        @Schema(description = "학습 완료 여부", example = "true")
        @NotNull(message = "marked 값은 필수입니다.")
        Boolean marked
) { }