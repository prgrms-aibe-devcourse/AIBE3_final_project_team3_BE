package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 생성 요청")
public record CommentCreateReq(
        @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "1")
        Long parentId,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(min = 1, max = 1000, message = "댓글은 1자 이상 1000자 이하로 입력해주세요.")
        @Schema(description = "내용", example = "좋은 글 감사합니다!")
        String content
) {}
