package triplestar.mixchat.global.security.redis;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class redisTokenRepository {

    private final String prefix;
    private final int expirationSeconds;
    private final StringRedisTemplate redisTemplate;

    public redisTokenRepository(
            @Value("${redis.prefix.refresh-token}")
            String prefix,
            @Value("${jwt.refresh-token-expiration-seconds}")
            int expirationSeconds,
            StringRedisTemplate redisTemplate
    ) {
        this.prefix = prefix;
        this.expirationSeconds = expirationSeconds;
        this.redisTemplate = redisTemplate;
    }

    public void save(Long memberId, String refreshToken) {
        String key = prefix + memberId;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(expirationSeconds));
    }

    public String findByMemberId(Long memberId) {
        String key = prefix + memberId;
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(Long memberId) {
        String key = prefix + memberId;
        redisTemplate.delete(key);
    }
}
