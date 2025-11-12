package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "게시글 수정(PATCH) 요청 바디 - 부분 수정 가능")
public record PostUpdateReq(
        @Schema(description = "수정할 제목 (부분 수정 허용, null이면 변경 없음)", example = "제목 수정 예시")
        @Nullable @Size(min = 1, max = 255)
        String title,

        @Schema(description = "수정할 내용 (null이면 변경 없음)", example = "내용 수정 예시")
        @Nullable @Size(min = 1)
        String content,

        @Schema(description = "최종적으로 유지/교체할 이미지 URL 전체 목록 (null이면 변경 없음, 빈 배열이면 모두 제거)", example = "['https://cdn.example.com/new1.jpg']")
        @Nullable
        List<@jakarta.validation.constraints.NotBlank String> imageUrls
) {}
