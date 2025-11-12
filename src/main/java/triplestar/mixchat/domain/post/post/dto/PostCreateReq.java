package triplestar.mixchat.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "게시글 생성 요청 바디")
public record PostCreateReq(
        @Schema(description = "게시글 제목", example = "여행 파트너를 찾습니다")
        @NotNull @NotBlank @Size(max = 255)
        String title,

        @Schema(description = "게시글 내용", example = "다음 달 일본 여행 같이 가실 분 구해요. 일정 공유 드립니다.")
        @NotNull @NotBlank
        String content,

        @Schema(description = "첨부 이미지 URL 목록 (없으면 null 또는 빈 배열)", example = "['https://cdn.example.com/img1.jpg','https://cdn.example.com/img2.jpg']")
        @Nullable
        List<@NotBlank String> imageUrls
) {}

