package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "게시글 상세 응답")
public record PostDetailResp(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "작성자 ID", example = "10")
        Long authorId,

        @Schema(description = "작성자 닉네임", example = "홍길동")
        String authorNickname,

        @Schema(description = "제목", example = "첫 번째 글")
        String title,

        @Schema(description = "내용", example = "안녕하세요")
        String content,

        @Schema(description = "이미지 URL 목록", example = "[\"https://example.com/image1.jpg\"]")
        List<String> imageUrls,

        @Schema(description = "조회수", example = "100")
        int viewCount,

        @Schema(description = "좋아요 수", example = "25")
        int likeCount,

        @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
        boolean isLiked,

        @Schema(description = "생성일시", example = "2024-12-01T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2024-12-04T15:20:00")
        LocalDateTime modifiedAt
) {
}

