package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "게시글 목록 조회 응답")
public record PostListResp(
        @Schema(description = "게시글 목록")
        @NotNull List<@NotNull PostListItemResp> items,
        @Schema(description = "전체 게시글 수", example = "125")
        @NotNull Long totalCount
) {
    public static PostListResp of(List<PostListItemResp> items, long totalCount) {
        return new PostListResp(items, totalCount);
    }
}

