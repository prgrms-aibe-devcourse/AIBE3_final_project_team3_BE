package triplestar.mixchat.domain.member.presence.repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

@Repository
public class PresenceRepository {

    private final StringRedisTemplate redisTemplate;
    private final String prefix;
    private final int expirationSeconds;

    private static final String SET_KEY_ONLINE_MEMBERS = "online_members";

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
        String key = getKey(memberId);
        redisTemplate.opsForValue().set(key, "online", Duration.ofSeconds(expirationSeconds));
        redisTemplate.opsForSet().add(SET_KEY_ONLINE_MEMBERS, memberId.toString());
    }

    private String getKey(Long memberId) {
        return prefix + memberId;
    }

    public Map<Long, Boolean> isOnlineBulk(List<Long> memberIds) {
        List<String> keys = memberIds.stream().map(this::getKey).toList();
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        HashMap<Long, Boolean> result = new HashMap<>(memberIds.size());

        if (values == null) {
            for (Long memberId : memberIds) {
                result.put(memberId, false);
            }
            return result;
        }

        for (int i = 0; i < memberIds.size(); i++) {
            result.put(memberIds.get(i), values.get(i) != null);
        }
        return result;
    }

    // Redis set을 사용하여 온라인 멤버 ID 목록을 가져오는 메서드
    public List<Long> getOnlineMemberIds(long offset, long size) {
        Set<String> onlineIds = redisTemplate.opsForSet()
                .members(SET_KEY_ONLINE_MEMBERS);

        if (onlineIds == null) {
            return List.of();
        }

        return onlineIds.stream()
                .skip(offset)
                .limit(size)
                .map(Long::valueOf)
                .toList();
    }

    @Scheduled(fixedRate = 30000) // 30초마다 실행
    public void cleanupExpiredPresences() {
        Set<String> onlineIds = redisTemplate.opsForSet().members(SET_KEY_ONLINE_MEMBERS);

        if (onlineIds == null) {
            return;
        }

        // TODO : redis 파이프라인 적용해 round-trip N -> 1로 줄이기
        onlineIds.forEach(id -> {
            Long memberId = Long.valueOf(id);
            String key = getKey(memberId);
            boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
            if (!exists) {
                redisTemplate.opsForSet().remove(SET_KEY_ONLINE_MEMBERS, id);
            }
        });
    }
}
