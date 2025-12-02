package triplestar.mixchat.domain.member.member.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import triplestar.mixchat.domain.member.member.entity.Member;

@Schema(description = "회원 상세 조회 응답 DTO")
public record MyProfileResp(
        @Schema(description = "회원 고유 ID", example = "123", requiredMode = REQUIRED)
        Long memberId,

        @Schema(description = "이메일", example = "email@.example.com", requiredMode = REQUIRED)
        String Email,

        @Schema(description = "이름", example = "홍길동", requiredMode = REQUIRED)
        String name,

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
        String profileImageUrl
) {
    public static MyProfileResp from(Member member) {
        return new MyProfileResp(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getCountry().name(),
                member.getEnglishLevel().name(),
                member.getInterests(),
                member.getDescription(),
                member.getProfileImageUrl()
        );
    }
}
