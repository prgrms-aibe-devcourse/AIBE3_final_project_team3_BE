package triplestar.mixchat.domain.member.presence.repository;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.presence.dto.ExpiredPresence;

@Repository
public class PresenceRepository {

    private final StringRedisTemplate redisTemplate;
    private final String key;
    private final int expirationSeconds;

    private static final DefaultRedisScript<List> CLEANUP_SCRIPT;

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

    static {
        CLEANUP_SCRIPT = new DefaultRedisScript<>();

        // 원자성 보장을 위한 Lua 스크립트: 만료된 항목 조회 및 삭제
        CLEANUP_SCRIPT.setScriptText("""
                    local expired = redis.call('ZRANGEBYSCORE', KEYS[1], 0, ARGV[1], 'WITHSCORES')
                    redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1])
                    return expired
                """);
        CLEANUP_SCRIPT.setResultType(List.class);
    }

    public void save(Long memberId) {
        long now = System.currentTimeMillis() / 1000;
        redisTemplate.opsForZSet().add(key, memberId.toString(), now);
    }

    public Set<Long> filterIsOnline(List<Long> memberIds) {
        long now = System.currentTimeMillis() / 1000;
        long threshold = now - expirationSeconds;

        if (memberIds == null || memberIds.isEmpty()) {
            return Set.of();
        }

        // ZMScore를 이용한 특정 key를 일괄 조회
        // RedisCallback을 사용하여 저수준 Redis 명령어 실행
        return redisTemplate.execute((RedisCallback<Set<Long>>) connection -> {
            RedisZSetCommands zSet = connection.zSetCommands();

            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

            byte[][] memberBytes = memberIds.stream()
                    .map(id -> id.toString().getBytes(StandardCharsets.UTF_8))
                    .toArray(byte[][]::new);

            // ZMScore 명령어로 특정 멤버들의 점수(접속 시간) 일괄 조회
            List<Double> scores = zSet.zMScore(keyBytes, memberBytes);

            if (scores == null || scores.isEmpty()) {
                return Set.of();
            }

            Set<Long> onlineIds = new HashSet<>();

            for (int i = 0; i < scores.size(); i++) {
                Double score = scores.get(i);
                if (score != null && score >= threshold) {
                    onlineIds.add(memberIds.get(i));
                }
            }
            return onlineIds;
        });
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

    public List<ExpiredPresence> cleanupExpired() {
        long now = System.currentTimeMillis() / 1000;
        long threshold = now - expirationSeconds;

        List<String> raw = (List<String>) redisTemplate.execute(
                CLEANUP_SCRIPT,
                List.of(key),
                String.valueOf(threshold)
        );

        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        List<ExpiredPresence> result = new ArrayList<>();

        // raw = ["id1", "score1", "id2", "score2", ...]
        for (int i = 0; i < raw.size(); i += 2) {
            Long memberId = Long.valueOf(raw.get(i));
            Long lastSeenAt = Long.valueOf(raw.get(i + 1));

            result.add(new ExpiredPresence(memberId, lastSeenAt));
        }

        return result;
    }

    public void remove(Long memberId) {
        redisTemplate.opsForZSet().remove(key, memberId.toString());
    }
}
