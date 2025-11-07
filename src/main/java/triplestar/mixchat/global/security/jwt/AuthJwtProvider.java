package triplestar.mixchat.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.member.member.constant.Role;

@Slf4j
@Component
public class AuthJwtProvider {

    private final int accessTokenExpiration;
    private final int refreshTokenExpiration;
    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public AuthJwtProvider(
            @Value("${jwt.access-token-expiration-seconds}") int accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration-seconds}") int refreshTokenExpiration,
            @Value("${jwt.access-secret}") String accessSecret,
            @Value("${jwt.refresh-secret}") String refreshSecret
    ) {
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.accessKey = convertKey(accessSecret);
        this.refreshKey = convertKey(refreshSecret);
    }

    private SecretKey convertKey(String keyString) {
        return Keys.hmacShaKeyFor(keyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 토큰 생성
     * */
    public String generateAccessToken(AccessTokenPayload payload) {
        Instant now = Instant.now();
        Date expiration = Date.from(now.plusSeconds(accessTokenExpiration));

        return Jwts.builder()
                .subject(payload.memberId().toString())
                .claim("role", payload.role().name())
                .expiration(expiration)
                .issuedAt(Date.from(now))
                .signWith(accessKey)
                .compact();
    }

    public String generateRefreshToken(Long memberId) {
        Instant now = Instant.now();
        Date expiration = Date.from(now.plusSeconds(refreshTokenExpiration));

        return Jwts.builder()
                .subject(memberId.toString())
                .expiration(expiration)
                .issuedAt(Date.from(now))
                .signWith(refreshKey)
                .compact();
    }

    /**
     * 토큰 파싱
     * */
    private Claims parseToken(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public AccessTokenPayload parseAccessToken(String token) {
        try {
            Claims claims = parseToken(token, accessKey);
            Long memberId = Long.parseLong(claims.getSubject());
            String role = claims.get("role", String.class);
            return new AccessTokenPayload(memberId, Role.valueOf(role));
        } catch (JwtException | NumberFormatException e) {
            log.warn(e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 JWT Token", e);
        }
    }

    public Long parseRefreshToken(String token) {
        try {
            Claims claims = parseToken(token, refreshKey);
            return Long.parseLong(claims.getSubject());
        } catch (JwtException | NumberFormatException e) {
            log.warn(e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 JWT Token", e);
        }
    }

    /**
     * 토큰 유효성 검사
     */
    private boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.info(e.getMessage());
            return false;
        }
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, accessKey);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshKey);
    }
}
