package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 수정 요청")
public record PostUpdateReq(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(min = 1, max = 255, message = "제목은 1자 이상 255자 이하로 입력해주세요.")
        @Schema(description = "제목", example = "수정된 제목")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(min = 1, max = 10000, message = "내용은 1자 이상 10000자 이하로 입력해주세요.")
        @Schema(description = "내용", example = "수정된 내용")
        String content
) {
}

