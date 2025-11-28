package triplestar.mixchat.domain.member.presence.repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PresenceRepository {

    private final StringRedisTemplate redisTemplate;
    private final String key;
    private final int expirationSeconds;

    public PresenceRepository(
            StringRedisTemplate redisTemplate,
            @Value("${presence.redis.key}")
            String key,
            @Value("${presence.redis.ttl-seconds}")
            int expirationSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.key = key;
        this.expirationSeconds = expirationSeconds;
    }

    public void save(Long memberId) {
        long now = System.currentTimeMillis() / 1000;
        redisTemplate.opsForZSet().add(key, memberId.toString(), now);
    }

    public Set<Long> filterOnlineBulk(List<Long> memberIds) {
        long now = System.currentTimeMillis() / 1000;
        long threshold = now - expirationSeconds;

        Set<String> onlineStrIds = redisTemplate.opsForZSet().rangeByScore(key, threshold, Double.MAX_VALUE);

        if (onlineStrIds == null || onlineStrIds.isEmpty()) {
            return Set.of();
        }

        Set<String> targetStrIds = memberIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toSet());

        // 교집합 계산
        onlineStrIds.retainAll(targetStrIds);

        if (onlineStrIds.isEmpty()) {
            return Set.of();
        }

        return onlineStrIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    public List<Long> getOnlineMemberIds(long offset, long size) {
        long now = System.currentTimeMillis() / 1000;
        long threshold = now - expirationSeconds;

        Set<String> onlineIds = redisTemplate.opsForZSet()
                .rangeByScore(key, threshold, Double.MAX_VALUE, offset, size);

        if (onlineIds == null) {
            return List.of();
        }

        return onlineIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    public void cleanupExpired() {
        long now = System.currentTimeMillis() / 1000;
        long threshold = now - expirationSeconds;

        redisTemplate.opsForZSet().removeRangeByScore(key, 0, threshold);
    }

    public void remove(Long memberId) {
        redisTemplate.opsForZSet().remove(key, memberId.toString());
    }
}
