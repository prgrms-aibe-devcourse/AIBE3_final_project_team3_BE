package triplestar.mixchat.domain.member.presence.repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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

    public Map<Long, Boolean> isOnlineBulk(List<Long> memberIds) {
        long now = System.currentTimeMillis() / 1000;
        long threshold = now - expirationSeconds;

        Map<Long, Boolean> result = new HashMap<>();
        Set<String> allOnlineIds = redisTemplate.opsForZSet().rangeByScore(key, threshold, Double.MAX_VALUE);

        if (allOnlineIds == null) {
            return result;
        }

        for (Long memberId : memberIds) {
            boolean isOnline = allOnlineIds.contains(memberId.toString());
            result.put(memberId, isOnline);
        }
        return result;
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
