package triplestar.mixchat.global.cache;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LearningNoteCacheRepository {

    private final StringRedisTemplate redis;
    private final String keyPrefix;
    private final Duration ttl;

    public LearningNoteCacheRepository(
            StringRedisTemplate redis,
            @Value("${redis.prefix.learning-search:learning:search:room:}") String keyPrefix,
            @Value("${redis.ttl.learning-search-minutes:10}") long ttlMinutes
    ) {
        this.redis = redis;
        this.keyPrefix = keyPrefix;  // 기본 prefix
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    private String key(Long roomId, Long userId) {
        return keyPrefix + roomId + ":user:" + userId;
    }

    /** 캐시 저장 */
    public void save(Long roomId, Long userId, List<Long> noteIds) {
        String value = String.join(",",
                noteIds.stream().map(String::valueOf).toList()
        );
        redis.opsForValue().set(key(roomId, userId), value, ttl);
    }

    /** 캐시 조회 */
    public List<Long> get(Long roomId, Long userId) {
        String v = redis.opsForValue().get(key(roomId, userId));
        if (v == null || v.isBlank()) return null;

        return Arrays.stream(v.split(","))
                .map(Long::valueOf)
                .toList();
    }

    /** 캐시 삭제 (방 나갈 때 가능) */
    public void delete(Long roomId, Long userId) {
        redis.delete(key(roomId, userId));
    }
}
