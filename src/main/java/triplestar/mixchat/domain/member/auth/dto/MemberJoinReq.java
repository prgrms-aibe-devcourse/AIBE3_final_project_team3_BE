package triplestar.mixchat.domain.member.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;

@Schema(description = "사용자의 회원가입 요청 정보")
public record MemberJoinReq(

        @Schema(description = "사용자 이메일 주소", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "비밀번호 (8자 이상, 대소문자/숫자 필수)",
                example = "P@sswOrd123")
        @NotBlank
        @Size(min = 8, max = 20)
        String password,

        @Schema(description = "비밀번호 확인 (password와 일치해야 함)")
        @NotBlank
        String passwordConfirm,

        @Schema(description = "실명")
        @NotBlank
        String name,

        @Schema(description = "국가 코드 (ISO 3166 Alpha-2)", example = "KR")
        @NotBlank
        String country,

        @Schema(description = "사용자 닉네임", example = "MixMaster")
        @NotBlank
        String nickname,

        @Schema(description = "영어 실력 레벨")
        @NotNull
        EnglishLevel englishLevel,

        @Schema(description = "관심사", example = "요리, 여행")
        @NotEmpty
        List<String> interests,

        @Schema(description = "자기소개")
        @NotBlank
        String description
) {
}