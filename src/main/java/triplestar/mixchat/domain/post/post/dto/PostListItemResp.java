package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "게시글 목록 조회 시 각 게시글 요약 정보")
public record PostListItemResp(
        @Schema(description = "게시글 ID", example = "10")
        @NotNull Long id,
        @Schema(description = "게시글 제목", example = "여행 메이트 구합니다")
        @NotNull String title,
        @Schema(description = "작성자 ID", example = "3")
        @NotNull Long authorId,
        @Schema(description = "작성자 닉네임", example = "TravelMate")
        @NotNull String authorNickname,
        @Schema(description = "조회수", example = "123")
        @NotNull Integer viewCount,
        @Schema(description = "좋아요 수", example = "7")
        @NotNull Integer likeCount,
        @Schema(description = "댓글 수", example = "15")
        @NotNull Integer commentCount,
        @Schema(description = "생성 시각 (ISO)", example = "2025-11-12T10:15:30")
        @NotNull LocalDateTime createdAt,
        @Schema(description = "대표 썸네일 URL (첫 이미지 없으면 null)", example = "https://cdn.example.com/thumb.jpg")
        @Nullable String thumbnailUrl
) {
    public static PostListItemResp of(Long id, String title, Long authorId, String authorNickname,
                                      int viewCount, int likeCount, int commentCount,
                                      LocalDateTime createdAt, String thumbnailUrl) {
        return new PostListItemResp(id, title, authorId, authorNickname, viewCount, likeCount, commentCount, createdAt, thumbnailUrl);
    }
}

