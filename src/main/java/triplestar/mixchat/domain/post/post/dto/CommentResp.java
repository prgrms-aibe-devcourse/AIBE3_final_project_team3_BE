package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "댓글 응답")
public record CommentResp(
        @Schema(description = "댓글 ID", example = "1")
        Long id,

        @Schema(description = "작성자 ID", example = "10")
        Long authorId,

        @Schema(description = "작성자 닉네임", example = "홍길동")
        String authorNickname,

        @Schema(description = "내용", example = "좋은 글 감사합니다!")
        String content,

        @Schema(description = "좋아요 수", example = "5")
        int likeCount,

        @Schema(description = "생성일시", example = "2024-12-01T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2024-12-04T15:20:00")
        LocalDateTime modifiedAt,

        @Schema(description = "대댓글 목록")
        List<CommentResp> replies
) {}

