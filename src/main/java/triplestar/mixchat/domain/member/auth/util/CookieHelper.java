package triplestar.mixchat.domain.member.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
public class CookieHelper {

    @Value("${jwt.refresh-token-expiration-seconds}")
    private int refreshTokenExpireSeconds;

    @Value("${cookie.domain}")
    private String cookieDomain;

    public Cookie generateRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("RefreshToken", refreshToken);

        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
        cookie.setDomain(cookieDomain);
        cookie.setMaxAge(refreshTokenExpireSeconds);

        return cookie;
    }

    public String findRefreshTokenCookie(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {
            throw new BadCredentialsException("리프레시 토큰이 존재하지 않습니다.");
        }

        return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("RefreshToken"))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("리프레시 토큰이 존재하지 않습니다."))
                .getValue();
    }

    public Cookie generateExpiredRefreshToken() {
        Cookie expiredCookie = new Cookie("RefreshToken", null);

        expiredCookie.setPath("/");
        expiredCookie.setHttpOnly(true);
        expiredCookie.setSecure(true);
        expiredCookie.setAttribute("SameSite", "None");
        expiredCookie.setDomain(cookieDomain);
        expiredCookie.setMaxAge(0);

        return expiredCookie;
    }
}
