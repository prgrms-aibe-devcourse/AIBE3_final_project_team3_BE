package triplestar.mixchat.domain.member.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;

@Schema(description = "사용자의 회원가입 요청 정보")
public record MemberJoinReq(

        @Schema(description = "사용자 이메일 주소", example = "user@example.com")
        @NotNull
        @Email
        String email,

        @Schema(description = "비밀번호 (8자 이상, 대소문자/숫자 필수)",
                example = "P@sswOrd123")
        @NotNull
        @Size(min = 8, max = 20)
        String password,

        @Schema(description = "비밀번호 확인 (password와 일치해야 함)")
        @NotNull
        String passwordConfirm,

        @Schema(description = "실명")
        @NotNull
        String name,

        @Schema(description = "국가 코드 (ISO 3166 Alpha-2)", example = "KR")
        @NotNull
        String country,

        @Schema(description = "사용자 닉네임", example = "MixMaster")
        @NotNull
        String nickname,

        @Schema(description = "영어 실력 레벨")
        @NotNull
        EnglishLevel englishLevel,

        @Schema(description = "관심사", example = "요리, 여행")
        @NotNull
        String interest,

        @Schema(description = "자기소개")
        @NotNull
        String description
) {
}