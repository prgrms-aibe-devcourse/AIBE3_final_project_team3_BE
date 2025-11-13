package triplestar.mixchat.domain.member.auth.dto;

public record SignInResp (
        String accessToken,
        String refreshToken
) {
}
