package triplestar.mixchat.global.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import triplestar.mixchat.global.jwt.AuthJwtProvider;

// 다음 PR 에서 마저 작성될 코드
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REFRESH_TOKEN = "RefreshToken";
    private final AuthJwtProvider authJwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            boolean isValid = authJwtProvider.validateAccessToken(token);
            if (isValid) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String refreshToken = findRefreshTokenOnCookie(request);

    }

    private String findRefreshTokenOnCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        return Arrays.stream(cookies).filter
                        (cookie -> cookie.getName().equals(REFRESH_TOKEN))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Refresh token is missing"))
                .getValue();
    }
}
