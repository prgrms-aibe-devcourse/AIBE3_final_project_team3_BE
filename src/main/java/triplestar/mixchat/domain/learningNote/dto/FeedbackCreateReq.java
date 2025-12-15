package triplestar.mixchat.domain.learningNote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.learningNote.constant.TranslationTagCode;

@Schema(description = "개별 피드백 항목")
public record FeedbackCreateReq(
        @NotNull
        @Schema(description = "피드백 태그", example = "Grammar")
        TranslationTagCode tag,

        @NotBlank
        @Schema(description = "문제가 있었던 원본 구절", example = "goes")
        String problem,

        @NotBlank
        @Schema(description = "수정 구절", example = "go")
        String correction,

        @NotBlank
        @Schema(description = "부가 설명", example = "시제 수정")
        String extra
) {}