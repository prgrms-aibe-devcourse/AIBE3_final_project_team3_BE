package triplestar.mixchat.domain.admin.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자용 문장 등록 요청")
public record AdminSentenceGameCreateReq(
        @NotNull
        @Schema(description = "학습노트 ID", example = "101")
        Long learningNoteId
) {}
