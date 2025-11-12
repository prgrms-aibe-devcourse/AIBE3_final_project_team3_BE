package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "게시글 생성 후 반환되는 기본 정보")
public record PostCreateResp(
        @Schema(description = "생성된 게시글 ID", example = "101")
        @NotNull
        Long id
) {
    public static PostCreateResp of(Long id) { return new PostCreateResp(id); }
}

