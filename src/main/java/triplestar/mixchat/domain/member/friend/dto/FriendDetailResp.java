package triplestar.mixchat.domain.member.friend.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;

@Schema(description = "친구 상세 조회 응답 DTO")
public record FriendDetailResp(
        @Schema(description = "회원 고유 ID", example = "123", requiredMode = REQUIRED)
        Long memberId,

        @Schema(description = "닉네임", example = "MixMaster", requiredMode = REQUIRED)
        String nickname,

        @Schema(description = "국가 코드 (Alpha-2)", example = "KR", requiredMode = REQUIRED)
        String country,

        @Schema(description = "영어 실력 레벨", example = "BEGINNER", requiredMode = REQUIRED)
        String englishLevel,

        @Schema(description = "관심사 목록", example = "[\"농구\", \"독서\"]", requiredMode = REQUIRED)
        List<String> interests,

        @Schema(description = "자기소개", example = "안녕하세요. 5년차 웹 개발자입니다.", requiredMode = REQUIRED)
        String description,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile/123_photo.jpg", requiredMode = REQUIRED)
        String profileImageUrl,

        @Schema(description = "친구관계 생성 날짜", example = "2024-07-25T10:30:00", requiredMode = REQUIRED)
        LocalDateTime createdAt
) {
}
