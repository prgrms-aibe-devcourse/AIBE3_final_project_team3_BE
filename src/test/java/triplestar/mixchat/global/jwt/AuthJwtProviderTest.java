package triplestar.mixchat.global.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import triplestar.mixchat.domain.member.member.constant.Role;
import triplestar.mixchat.global.security.jwt.AccessTokenPayload;
import triplestar.mixchat.global.security.jwt.AuthJwtProvider;

@ActiveProfiles("test")
@DisplayName("jwt 생성/파싱(검증) 테스트")
class AuthJwtProviderTest {

    AuthJwtProvider authJwtProvider = new AuthJwtProvider(
            5,
            60,
            "abcdefghijklmnopqrstuvwxyz123456",
            "abcdefghijklmnopqrstuvwxyz654321"
    );

    @Test
    @DisplayName("토큰 생성 및 파싱")
    void jwt_success() {
        String accessToken = authJwtProvider.generateAccessToken(new AccessTokenPayload(1L, Role.ROLE_MEMBER));

        AccessTokenPayload payload = authJwtProvider.parseAccessToken(accessToken);
        assertThat(payload.memberId()).isEqualTo(1L);
        assertThat(payload.role()).isEqualTo(Role.ROLE_MEMBER);
    }

    @Test
    @DisplayName("위조된 토큰 파싱시 예외 발생")
    void validate_counterfeit() {
        String accessToken = authJwtProvider.generateAccessToken(new AccessTokenPayload(1L, Role.ROLE_MEMBER));

        Assertions.assertThatThrownBy(() -> authJwtProvider.parseAccessToken("abc" + accessToken))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("만료된 토큰 파싱시 예외 발생")
    void validate_fail() throws InterruptedException {
        String accessToken = authJwtProvider.generateAccessToken(new AccessTokenPayload(1L, Role.ROLE_MEMBER));

        Thread.sleep(1000 * 7);

        Assertions.assertThatThrownBy(() -> authJwtProvider.parseAccessToken(accessToken))
                .isInstanceOf(BadCredentialsException.class);
    }
}