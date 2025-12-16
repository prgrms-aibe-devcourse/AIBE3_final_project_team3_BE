package triplestar.mixchat.domain.admin.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자용 게시글 삭제 응답")
public record AdminPostDeleteResp(
        @Schema(description = "게시글 ID", example = "1", requiredMode = REQUIRED)
        Long postId
) {}