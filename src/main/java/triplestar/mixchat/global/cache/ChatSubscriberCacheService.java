package triplestar.mixchat.global.cache;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 현재 채팅방에 WebSocket으로 구독 중인 사용자 관리
 *
 * 하이브리드 방식:
 * 1. 세션별 구독 관리 (sessions Set) - 각 기기 독립적 관리
 * 2. 멤버별 집계 (members Set) - 빠른 조회용 캐시
 * 3. 세션-멤버 매핑 (session:member) - 세션으로 멤버 조회
 *
 * 이 방식으로 한 유저가 여러 기기에서 접속해도 정확히 처리됨
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSubscriberCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    // 서버 시작 시 모든 구독자 정보 초기화 (유령 세션 방지)
    @PostConstruct
    public void clearAllSubscribersOnStartup() {
        try {
            Set<String> keys = redisTemplate.keys("chat:subscribers:room:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("서버 시작 - 구독자 캐시 초기화 완료: {}개 키 삭제", keys.size());
            }

            Set<String> sessionKeys = redisTemplate.keys("session:member:*");
            if (sessionKeys != null && !sessionKeys.isEmpty()) {
                redisTemplate.delete(sessionKeys);
                log.info("서버 시작 - 세션 매핑 캐시 초기화 완료: {}개 키 삭제", sessionKeys.size());
            }
        } catch (Exception e) {
            log.error("Redis 캐시 초기화 실패", e);
        }
    }

    private static final String SESSIONS_KEY_PREFIX = "chat:subscribers:room:";
    private static final String SESSIONS_KEY_SUFFIX = ":sessions";
    private static final String MEMBERS_KEY_SUFFIX = ":members";
    private static final String SESSION_MEMBER_MAPPING_PREFIX = "session:member:";

    private String getSessionsKey(Long roomId) {
        return SESSIONS_KEY_PREFIX + roomId + SESSIONS_KEY_SUFFIX;
    }

    private String getMembersKey(Long roomId) {
        return SESSIONS_KEY_PREFIX + roomId + MEMBERS_KEY_SUFFIX;
    }

    private String getSessionMappingKey(String sessionId) {
        return SESSION_MEMBER_MAPPING_PREFIX + sessionId;
    }

    /**
     * 채팅방 구독 시작
     * @param roomId 채팅방 ID
     * @param memberId 회원 ID
     * @param sessionId WebSocket 세션 ID
     */
    public void addSubscriber(Long roomId, Long memberId, String sessionId) {
        String sessionsKey = getSessionsKey(roomId);
        String membersKey = getMembersKey(roomId);
        String mappingKey = getSessionMappingKey(sessionId);

        // 1. 세션별 구독 추가
        redisTemplate.opsForSet().add(sessionsKey, sessionId);

        // 2. 세션-멤버 매핑 저장
        redisTemplate.opsForValue().set(mappingKey, String.valueOf(memberId));

        // 3. 멤버별 집계에 추가 (Set이므로 중복 자동 제거)
        redisTemplate.opsForSet().add(membersKey, String.valueOf(memberId));
    }

    /**
     * 채팅방 구독 해제
     * @param roomId 채팅방 ID
     * @param memberId 회원 ID
     * @param sessionId WebSocket 세션 ID
     */
    public void removeSubscriber(Long roomId, Long memberId, String sessionId) {
        String sessionsKey = getSessionsKey(roomId);
        String membersKey = getMembersKey(roomId);
        String mappingKey = getSessionMappingKey(sessionId);

        // 1. 세션별 구독 제거
        Long removedSession = redisTemplate.opsForSet().remove(sessionsKey, sessionId);

        // 2. 이 회원의 다른 세션이 남아있는지 확인 (매핑 삭제 전에 확인)
        boolean hasOtherSessions = checkOtherSessionsForMember(roomId, memberId);

        // 3. 세션-멤버 매핑 제거 (확인 후 삭제)
        redisTemplate.delete(mappingKey);

        // 4. 모든 세션이 해제되었으면 members Set에서도 제거
        if (!hasOtherSessions) {
            redisTemplate.opsForSet().remove(membersKey, String.valueOf(memberId));
        }
    }

    /**
     * 현재 구독 중인지 확인 (빠른 조회용 - O(1))
     * @param roomId 채팅방 ID
     * @param memberId 회원 ID
     * @return 구독 중이면 true
     */
    public boolean isSubscribed(Long roomId, Long memberId) {
        String membersKey = getMembersKey(roomId);
        Boolean isMember = redisTemplate.opsForSet().isMember(membersKey, String.valueOf(memberId));
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 현재 구독 중인 모든 사용자 조회 (members Set 기반)
     * @param roomId 채팅방 ID
     * @return 구독 중인 회원 ID Set
     */
    public Set<String> getSubscribers(Long roomId) {
        String membersKey = getMembersKey(roomId);
        return redisTemplate.opsForSet().members(membersKey);
    }

    /**
     * 현재 구독 중인 모든 세션 조회
     * @param roomId 채팅방 ID
     * @return 구독 중인 세션 ID Set
     */
    public Set<String> getSubscribedSessions(Long roomId) {
        String sessionsKey = getSessionsKey(roomId);
        return redisTemplate.opsForSet().members(sessionsKey);
    }

    /**
     * 특정 회원의 다른 세션이 남아있는지 확인
     * @param roomId 채팅방 ID
     * @param memberId 회원 ID
     * @return 다른 세션이 있으면 true
     */
    private boolean checkOtherSessionsForMember(Long roomId, Long memberId) {
        String sessionsKey = getSessionsKey(roomId);
        Set<String> allSessions = redisTemplate.opsForSet().members(sessionsKey);

        if (allSessions == null || allSessions.isEmpty()) {
            return false;
        }

        // 모든 세션을 확인하여 같은 memberId를 가진 세션이 있는지 검사
        for (String sessionId : allSessions) {
            String mappingKey = getSessionMappingKey(sessionId);
            String sessionMemberId = redisTemplate.opsForValue().get(mappingKey);

            if (sessionMemberId != null && sessionMemberId.equals(String.valueOf(memberId))) {
                return true; // 같은 회원의 다른 세션 발견
            }
        }

        return false;
    }

    /**
     * 특정 세션의 회원 ID 조회
     * @param sessionId 세션 ID
     * @return 회원 ID (없으면 null)
     */
    public Long getMemberIdBySessionId(String sessionId) {
        String mappingKey = getSessionMappingKey(sessionId);
        String memberIdStr = redisTemplate.opsForValue().get(mappingKey);
        return memberIdStr != null ? Long.parseLong(memberIdStr) : null;
    }

    /**
     * 특정 회원의 모든 세션 ID 조회
     * @param roomId 채팅방 ID
     * @param memberId 회원 ID
     * @return 세션 ID Set
     */
    public Set<String> getSessionsByMemberId(Long roomId, Long memberId) {
        String sessionsKey = getSessionsKey(roomId);
        Set<String> allSessions = redisTemplate.opsForSet().members(sessionsKey);

        if (allSessions == null || allSessions.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> memberSessions = new HashSet<>();
        for (String sessionId : allSessions) {
            String mappingKey = getSessionMappingKey(sessionId);
            String sessionMemberId = redisTemplate.opsForValue().get(mappingKey);

            if (sessionMemberId != null && sessionMemberId.equals(String.valueOf(memberId))) {
                memberSessions.add(sessionId);
            }
        }

        return memberSessions;
    }
}
