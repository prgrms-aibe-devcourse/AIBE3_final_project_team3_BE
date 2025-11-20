package triplestar.mixchat.domain.member.member.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;

@Schema(description = "회원 프로필 상세 조회 응답 DTO")
public record MemberDetailResp(
        @Schema(description = "회원 고유 ID", example = "123", requiredMode = REQUIRED)
        Long memberId,

        @Schema(description = "이메일 주소", example = "gildong@example.com", requiredMode = REQUIRED)
        String email,

        @Schema(description = "실명", example = "홍길동", requiredMode = REQUIRED)
        String name,

        @Schema(description = "닉네임", example = "MixMaster", requiredMode = REQUIRED)
        String nickname,

        @Schema(description = "국가 코드 (Alpha-2)", example = "KR", requiredMode = REQUIRED)
        Country country,

        @Schema(description = "영어 실력 레벨", example = "BEGINNER", requiredMode = REQUIRED)
        EnglishLevel englishLevel,

        @Schema(description = "관심사 목록", example = "[\"농구\", \"독서\"]", requiredMode = REQUIRED)
        List<String> interests,

        @Schema(description = "자기소개", example = "안녕하세요. 5년차 웹 개발자입니다.", requiredMode = REQUIRED)
        String description,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile/123_photo.jpg", requiredMode = REQUIRED)
        String profileImageUrl,

        @Schema(description = "현재 로그인 사용자와 친구 관계인지 여부", example = "true", requiredMode = REQUIRED)
        Boolean isFriend,

        @Schema(description = "현재 로그인 사용자가 상대방에게 친구 요청을 보내고 대기 중인 상태인지 여부",
                example = "false", requiredMode = REQUIRED)
        Boolean isPendingFriendRequestFromMe,

        @Schema(description = "현재 로그인 사용자가 상대방으로부터 친구 요청을 받고 대기 중인 상태인지 여부",
                example = "false", requiredMode = REQUIRED)
        Boolean isPendingFriendRequestFromOpponent
) {
        public static MemberDetailResp forAnonymousViewer(Member member) {
                return new MemberDetailResp(
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
                        false,
                        false
                );
        }
}
