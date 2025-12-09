package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 상태 응답")
public record LikeStatusResp(
        @Schema(description = "좋아요 여부", example = "true")
        boolean liked,

        @Schema(description = "총 좋아요 수", example = "25")
        int likeCount
) {}

