package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import triplestar.mixchat.domain.post.post.entity.Post;

@Schema(description = "게시글 상세 조회 응답")
public record PostDetailResp(
        @Schema(description = "게시글 ID", example = "10")
        @NotNull Long id,
        @Schema(description = "게시글 제목", example = "여행 메이트 구합니다")
        @NotNull String title,
        @Schema(description = "게시글 내용", example = "다음 달 일본 여행 일정 공유합니다...")
        @NotNull String content,
        @Schema(description = "작성자 ID", example = "3")
        @NotNull Long authorId,
        @Schema(description = "작성자 닉네임", example = "TravelMate")
        @NotNull String authorNickname,
        @Schema(description = "조회수", example = "124")
        @NotNull Integer viewCount,
        @Schema(description = "좋아요 수", example = "7")
        @NotNull Integer likeCount,
        @Schema(description = "댓글 수", example = "15")
        @NotNull Integer commentCount,
        @Schema(description = "이미지 URL 목록 (없으면 빈 배열)", example = "['https://cdn.example.com/img1.jpg']")
        @NotNull List<@NotNull String> imageUrls,
        @Schema(description = "생성 시각", example = "2025-11-12T10:15:30")
        @NotNull LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2025-11-12T11:00:00")
        @Nullable LocalDateTime modifiedAt
) {
    public static PostDetailResp of(Post post, int likeCount, int commentCount, List<String> imageUrls) {
        return new PostDetailResp(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getViewCount(),
                likeCount,
                commentCount,
                imageUrls,
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}
