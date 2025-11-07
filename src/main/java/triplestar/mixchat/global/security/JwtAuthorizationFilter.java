package triplestar.mixchat.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import triplestar.mixchat.global.jwt.AccessTokenPayload;
import triplestar.mixchat.global.jwt.AuthJwtProvider;

// AccessToken 기반 인증 처리 필터
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthJwtProvider authJwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            boolean isValid = authJwtProvider.validateAccessToken(token);
            if (isValid) {
                AuthenticateByToken(token);
            } else {
                throw new BadCredentialsException("유효하지 않은 액세스 토큰입니다.");
            }
        }

        filterChain.doFilter(request, response);
    }

    private void AuthenticateByToken(String token) {
        AccessTokenPayload accessTokenPayload = authJwtProvider.parseAccessToken(token);
        UserDetails userDetails = customUserDetailsService
                .loadUserByUsername(accessTokenPayload.memberId().toString());

        Authentication authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
