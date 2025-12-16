package triplestar.mixchat.domain.member.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
<<<<<<<< HEAD:src/main/java/triplestar/mixchat/domain/member/auth/dto/SignUpReq.java
import triplestar.mixchat.domain.member.member.constant.Country;
========
>>>>>>>> d5b65124bc6edc83c20ddfd69a8cb4b0951c0521:src/main/java/triplestar/mixchat/domain/member/auth/dto/MemberJoinReq.java
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;

@Schema(description = "사용자의 회원가입 요청 정보")
public record SignUpReq(

        @Schema(description = "사용자 이메일 주소", example = "user@example.com")
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Schema(description = "비밀번호 (8자 이상, 대소문자/숫자 필수)", example = "P@sswOrd123")
        @NotBlank
        @Size(min = 8, max = 20)
        String password,

        @Schema(description = "비밀번호 확인 (password와 일치해야 함)", example = "P@sswOrd123")
        @NotBlank
        String passwordConfirm,

        @Schema(description = "실명")
        @NotBlank
<<<<<<<< HEAD:src/main/java/triplestar/mixchat/domain/member/auth/dto/SignUpReq.java
        @Size(max = 50)
        String name,

        @Schema(description = "국가 코드 (ISO 3166 Alpha-2)", example = "KR")
        @NotNull
        Country country,

        @Schema(description = "사용자 닉네임", example = "MixMaster")
        @NotBlank
        @Size(max = 50)
========
        String name,

        @Schema(description = "국가 코드 (ISO 3166 Alpha-2)", example = "KR")
        @NotBlank
        String country,

        @Schema(description = "사용자 닉네임", example = "MixMaster")
        @NotBlank
>>>>>>>> d5b65124bc6edc83c20ddfd69a8cb4b0951c0521:src/main/java/triplestar/mixchat/domain/member/auth/dto/MemberJoinReq.java
        String nickname,

        @Schema(description = "영어 실력 레벨")
        @NotNull
        EnglishLevel englishLevel,

        @Schema(description = "관심사 목록 (최소 1개 이상 필수)", example = "[\"요리\", \"여행\"]")
        @NotEmpty
<<<<<<<< HEAD:src/main/java/triplestar/mixchat/domain/member/auth/dto/SignUpReq.java
        @Size(min = 1, max = 10)
        List<@NotBlank @Size(max = 30) String> interests,

        @Schema(description = "자기소개")
        @NotBlank
        @Size(max = 1000)
========
        List<String> interests,

        @Schema(description = "자기소개")
        @NotBlank
>>>>>>>> d5b65124bc6edc83c20ddfd69a8cb4b0951c0521:src/main/java/triplestar/mixchat/domain/member/auth/dto/MemberJoinReq.java
        String description
) {
}