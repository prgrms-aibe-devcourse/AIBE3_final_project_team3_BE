package triplestar.mixchat.global.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ChatAuthCacheService {

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;
    private final Duration ttl;

    public ChatAuthCacheService(
            StringRedisTemplate redisTemplate,
            @Value("${redis.prefix.chat-auth:chat:auth:room:}") String keyPrefix,
            @Value("${redis.ttl.chat-auth-hours:1}") long ttlHours
    ) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
        this.ttl = Duration.ofHours(ttlHours);
    }

    /**
     * 사용자가 특정 채팅방의 멤버인지 캐시를 통해 확인합니다.
     * Redis SET의 SISMEMBER 연산을 사용합니다.
     *
     * @param roomId 확인할 채팅방 ID
     * @param userId 확인할 사용자 ID
     * @return 캐시에 멤버로 존재하면 true, 아니면 false
     */
    public boolean isMember(Long roomId, Long userId) {
        String key = createKey(roomId);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, String.valueOf(userId)));
    }

    /**
     * 특정 채팅방에 사용자를 멤버로 캐시에 추가합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 추가할 사용자 ID
     */
    public void addMember(Long roomId, Long userId) {
        String key = createKey(roomId);
        redisTemplate.opsForSet().add(key, String.valueOf(userId));
        redisTemplate.expire(key, ttl); // 캐시 유효 시간 설정
    }

    /**
     * Redis 키를 생성합니다.
     * 예: "chat:auth:room:123:members"
     *
     * @param roomId 채팅방 ID
     * @return 생성된 Redis 키
     */
    private String createKey(Long roomId) {
        return keyPrefix + roomId + ":members";
    }
}
