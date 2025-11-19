package triplestar.mixchat.domain.member.auth.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 DTO: 인증 토큰 정보")
public record LogInResp(
        @Schema(description = "액세스 토큰 (Bearer 타입)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxMjMiLCJyb2xlIj"
                        + "oiUk9MRV9NRU1CRVIiLCJpYXQiOjE2NzIxMDc2MDAsImV4cCI6MTY3MjEwOTQwMH0.XXXXX",
                requiredMode = REQUIRED)
        String accessToken,

        @Schema(description = "리프레시 토큰",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxMjMiLCJleHAiOjE2NzIxOTgwMDB9.YYYYY",
                requiredMode = REQUIRED)
        String refreshToken
) {
}