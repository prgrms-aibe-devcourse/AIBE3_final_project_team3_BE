package triplestar.mixchat.global.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

// 현재 채팅방에 WebSocket으로 구독 중인 사용자 관리
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSubscriberCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String SUBSCRIBER_KEY_PREFIX = "chat:subscribers:";

    private String getKey(Long roomId) {
        return SUBSCRIBER_KEY_PREFIX + roomId;
    }

    // 채팅방 구독 시작
    public void addSubscriber(Long roomId, Long memberId) {
        String key = getKey(roomId);
        redisTemplate.opsForSet().add(key, String.valueOf(memberId));
        log.debug("Added subscriber: roomId={}, memberId={}", roomId, memberId);
    }

    // 채팅방 구독 해제
    public void removeSubscriber(Long roomId, Long memberId) {
        String key = getKey(roomId);
        redisTemplate.opsForSet().remove(key, String.valueOf(memberId));
        log.debug("Removed subscriber: roomId={}, memberId={}", roomId, memberId);
    }

    // 현재 구독 중인지 확인
    public boolean isSubscribed(Long roomId, Long memberId) {
        String key = getKey(roomId);
        Boolean isMember = redisTemplate.opsForSet().isMember(key, String.valueOf(memberId));
        return Boolean.TRUE.equals(isMember);
    }

    // 현재 구독 중인 모든 사용자 조회
    public Set<String> getSubscribers(Long roomId) {
        String key = getKey(roomId);
        return redisTemplate.opsForSet().members(key);
    }
}
