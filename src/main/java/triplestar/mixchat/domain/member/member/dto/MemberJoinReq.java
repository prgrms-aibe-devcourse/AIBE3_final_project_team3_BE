package triplestar.mixchat.domain.member.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberJoinReq(
        @NotNull
        @Email
        String email,
        @NotNull
        @Size(min = 8, max = 20)
        String password,
        @NotNull
        String passwordConfirm,
        @NotNull
        String name,
        @NotNull
        String country,
        @NotNull
        String nickname,
        @NotNull
        String englishLevel,
        @NotNull
        String interest,
        @NotNull
        String description
) {
}
