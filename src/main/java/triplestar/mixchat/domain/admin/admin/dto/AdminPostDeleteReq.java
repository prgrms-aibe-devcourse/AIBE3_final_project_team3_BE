package triplestar.mixchat.domain.admin.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자용 게시글 삭제 요청")
public record AdminPostDeleteReq(
        @Schema(description = "삭제 사유", example = "2")
        @NotNull
        Integer reasonCode
) {}