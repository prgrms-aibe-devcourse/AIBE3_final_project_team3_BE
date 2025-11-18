package triplestar.mixchat.domain.admin.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자용 문장 등록 요청")
public record AdminSentenceGameCreateReq(
        @Schema(description = "수정 전 문장", example = "I goed to the store yesterday.")
        @NotBlank
        String originalContent,

        @Schema(description = "수정 후 문장", example = "I went to the store yesterday.")
        @NotBlank
        String correctedContent
) {}
