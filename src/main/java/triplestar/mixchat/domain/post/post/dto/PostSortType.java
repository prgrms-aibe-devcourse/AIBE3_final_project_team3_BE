package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 정렬 타입", enumAsRef = true)
public enum PostSortType {
    @Schema(description = "최신순")
    LATEST,
    @Schema(description = "인기순")
    POPULAR
}

