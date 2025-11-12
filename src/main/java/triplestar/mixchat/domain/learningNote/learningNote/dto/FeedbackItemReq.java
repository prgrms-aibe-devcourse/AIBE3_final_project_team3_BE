package triplestar.mixchat.domain.learningNote.learningNote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;

@Schema(description = "개별 피드백 항목")
public record FeedbackItemReq(
        @NotNull
        @Schema(description = "피드백 태그", example = "Grammar")
        TranslationTagCode tag,

        @NotNull
        @Schema(description = "문제가 있었던 원본 구절", example = "goes")
        String problem,

        @NotNull
        @Schema(description = "수정 구절", example = "go")
        String correction,

        @NotNull
        @Schema(description = "부가 설명", example = "")
        String extra
) {}