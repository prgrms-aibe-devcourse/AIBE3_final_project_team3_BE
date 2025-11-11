package triplestar.mixchat.domain.member.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberSignInReq(
        @Schema(description = "사용자 이메일 주소", example = "user@example.com")
        @NotNull
        @Email
        String email,

        @Schema(description = "비밀번호 (8자 이상, 대소문자/숫자 필수)",
                example = "P@sswOrd123")
        @NotNull
        @Size(min = 8, max = 20)
        String password
) {
}
