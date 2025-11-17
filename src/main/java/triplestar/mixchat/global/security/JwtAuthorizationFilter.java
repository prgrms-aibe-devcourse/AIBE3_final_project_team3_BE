package triplestar.mixchat.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import triplestar.mixchat.global.security.jwt.AccessTokenPayload;
import triplestar.mixchat.global.security.jwt.AuthJwtProvider;

// AccessToken 기반 인증 처리 필터
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    // 표준화 되어있기 때문에 @Value 불필요?
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthJwtProvider authJwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String header = request.getHeader(AUTHORIZATION_HEADER);

            if (header != null && header.startsWith(BEARER_PREFIX)) {
                String token = header.substring(BEARER_PREFIX.length());
                AuthenticateByToken(token);
            }
            filterChain.doFilter(request, response);
        } catch (BadCredentialsException e) {
            log.warn("유효하지 않은 토큰. 요청 URI : {}, {}",request.getRequestURI(),  e.getMessage());

            // filter에 발생한 예외는 ControllerAdvice에 잡히지 않으므로 여기서 직접 응답 처리
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("유효하지 않은 JWT Token");
        }
    }

    private void AuthenticateByToken(String token) {
        AccessTokenPayload payload = authJwtProvider.parseAccessToken(token);

        Authentication authToken =
                new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(payload.memberId(), payload.role()),
                        null,
                        List.of(new SimpleGrantedAuthority(payload.role().name()))
                );

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
