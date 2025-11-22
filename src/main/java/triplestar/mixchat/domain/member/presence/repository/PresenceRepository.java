package triplestar.mixchat.domain.member.presence.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PresenceRepository {

    private final StringRedisTemplate redisTemplate;
    private final String prefix;
    private final int expirationSeconds;

    public PresenceRepository(
            StringRedisTemplate redisTemplate,
            @Value("${redis.prefix.presence-user}")
            String prefix,
            @Value("${redis.expiration.presence-user}")
            int expirationSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.prefix = prefix;
        this.expirationSeconds = expirationSeconds;
    }

    public void save(Long memberId) {
        String key = prefix + memberId;
        redisTemplate.opsForValue().set(key, "online", expirationSeconds);
    }

    public boolean isOnline(Long memberId) {
        String key = prefix + memberId;
        return redisTemplate.hasKey(key);
    }

    public void delete(Long memberId) {
        String key = prefix + memberId;
        redisTemplate.delete(key);
    }
}
