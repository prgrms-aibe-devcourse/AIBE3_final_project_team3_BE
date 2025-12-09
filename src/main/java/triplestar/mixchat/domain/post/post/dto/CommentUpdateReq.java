package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 수정 요청")
public record CommentUpdateReq(
        @NotBlank(message = "내용은 필수입니다.")
        @Size(min = 1, max = 1000, message = "댓글은 1자 이상 1000자 이하로 입력해주세요.")
        @Schema(description = "내용", example = "수정된 댓글 내용입니다.")
        String content
) {}

