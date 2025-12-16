package triplestar.mixchat.domain.member.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 요청 DTO")
public record LogInReq(
        @Schema(description = "사용자 이메일 주소", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "비밀번호 (8자 이상, 대소문자/숫자 필수)",
                example = "P@sswOrd123")
        @NotBlank
        @Size(min = 8, max = 20)
        String password
) {
}
