package triplestar.mixchat.domain.member.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;

@Schema(description = "회원 프로필 상세 조회 응답 DTO")
public record MemberProfileResp(
        @Schema(description = "회원 고유 ID", example = "123")
        @NotNull
        Long memberId,

        @Schema(description = "이메일 주소", example = "gildong@example.com")
        @NotBlank
        String email,

        @Schema(description = "실명", example = "홍길동")
        @NotBlank
        String name,

        @Schema(description = "닉네임", example = "MixMaster")
        @NotBlank
        String nickname,

        @Schema(description = "국가 (Full Name)", example = "Korea")
        @NotNull
        Country country,

        @Schema(description = "영어 실력 레벨", example = "BEGINNER")
        @NotNull
        EnglishLevel englishLevel,

        @Schema(description = "관심사 목록", example = "[\"농구\", \"독서\"]")
        @NotEmpty
        List<String> interests,

        @Schema(description = "자기소개", example = "안녕하세요. 5년차 웹 개발자입니다.")
        @NotBlank
        String description,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile/123_photo.jpg")
        @NotBlank
        String profileImageUrl,

        @Schema(description = "현재 로그인 사용자와 친구 관계인지 여부", example = "true")
        @NotNull
        Boolean isFriend,

        @Schema(description = "현재 로그인 사용자가 상대에게 친구 신청을 보냈거나 받았는지 대기 중인 상태인지 여부", example = "false")
        @NotNull
        Boolean isPendingRequest
) {
        public static MemberProfileResp forAnonymousViewer(Member member) {
                return new MemberProfileResp(
                        member.getId(),
                        member.getEmail(),
                        member.getName(),
                        member.getNickname(),
                        member.getCountry(),
                        member.getEnglishLevel(),
                        member.getInterests(),
                        member.getDescription(),
                        member.getProfileImageUrl(),
                        false,
                        false
                );
        }
}
