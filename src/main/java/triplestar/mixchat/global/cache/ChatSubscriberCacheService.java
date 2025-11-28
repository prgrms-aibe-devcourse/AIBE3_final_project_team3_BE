package triplestar.mixchat.global.cache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
 * TTL 기반 유령 세션 자동 정리:
 * - 구독 시 1시간 TTL 설정
 * - 정상 연결: disconnect 이벤트로 즉시 제거
 * - 유령 세션: 1시간 후 자동 만료
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSubscriberCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    // 구독자 정보 TTL: 12시간 (유령 세션 자동 정리, 일반 사용에는 충분한 시간)
    private static final long SUBSCRIBER_TTL_SECONDS = 3600 * 12;

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
        redisTemplate.expire(sessionsKey, SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);

        // 2. 세션-멤버 매핑 저장
        redisTemplate.opsForValue().set(mappingKey, String.valueOf(memberId), SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);

        // 3. 멤버별 집계에 추가 (Set이므로 중복 자동 제거)
        redisTemplate.opsForSet().add(membersKey, String.valueOf(memberId));
        redisTemplate.expire(membersKey, SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);
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
