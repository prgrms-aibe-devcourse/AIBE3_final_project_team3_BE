package triplestar.mixchat.global.cache;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 현재 채팅방에 WebSocket으로 구독 중인 사용자 관리
 * 하이브리드 방식:
 * 1. 세션별 구독 관리 (sessions Set) - 각 기기 독립적 관리
 * 2. 멤버별 집계 (members Set) - 빠른 조회용 캐시
 * 3. 세션-멤버 매핑 (session:member) - 세션으로 멤버 조회
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
    // 역방향 인덱스: 세션이 구독 중인 방 목록 (session -> set of roomIds)
    private static final String SESSION_ROOMS_KEY_PREFIX = "chat:session:rooms:";
    // 역방향 인덱스: 회원의 활성 세션 목록 (member -> set of sessionIds)
    private static final String MEMBER_SESSIONS_KEY_PREFIX = "chat:member:sessions:";

    private String getSessionsKey(Long roomId) {
        return SESSIONS_KEY_PREFIX + roomId + SESSIONS_KEY_SUFFIX;
    }

    private String getMembersKey(Long roomId) {
        return SESSIONS_KEY_PREFIX + roomId + MEMBERS_KEY_SUFFIX;
    }

    private String getSessionMappingKey(String sessionId) {
        return SESSION_MEMBER_MAPPING_PREFIX + sessionId;
    }

    private String getSessionRoomsKey(String sessionId) {
        return SESSION_ROOMS_KEY_PREFIX + sessionId;
    }
    
    private String getMemberSessionsKey(Long memberId) {
        return MEMBER_SESSIONS_KEY_PREFIX + memberId;
    }

    // 채팅방 구독 시작
    public void addSubscriber(Long roomId, Long memberId, String sessionId) {
        String sessionsKey = getSessionsKey(roomId);
        String membersKey = getMembersKey(roomId);
        String mappingKey = getSessionMappingKey(sessionId);
        String sessionRoomsKey = getSessionRoomsKey(sessionId);
        String memberSessionsKey = getMemberSessionsKey(memberId);

        // Redis 왕복을 줄이기 위해 세션/멤버 추가와 TTL 설정을 파이프라인 처리
        redisTemplate.executePipelined(new SessionCallback<Void>() {
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> Void execute(RedisOperations<K, V> operations) throws DataAccessException {
                RedisOperations<String, String> redisOps = (RedisOperations<String, String>) operations;

                // 1. 방별 세션 목록에 추가
                redisOps.opsForSet().add(sessionsKey, sessionId);
                redisOps.expire(sessionsKey, SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);

                // 2. 세션 -> 회원 ID 매핑 저장
                redisOps.opsForValue().set(mappingKey, String.valueOf(memberId),
                        SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);

                // 3. 방별 회원 목록(집계)에 추가
                redisOps.opsForSet().add(membersKey, String.valueOf(memberId));
                redisOps.expire(membersKey, SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);

                // 4. 세션 -> 구독 방 목록에 추가 (역방향 인덱스)
                redisOps.opsForSet().add(sessionRoomsKey, String.valueOf(roomId));
                redisOps.expire(sessionRoomsKey, SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);

                // 5. 회원 -> 활성 세션 목록에 추가 (역방향 인덱스)
                redisOps.opsForSet().add(memberSessionsKey, sessionId);
                redisOps.expire(memberSessionsKey, SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);

                return null;
            }
        });
    }

    // 채팅방 구독 해제
    public void removeSubscriber(Long roomId, Long memberId, String sessionId) {
        String sessionsKey = getSessionsKey(roomId);
        String membersKey = getMembersKey(roomId);
        String sessionRoomsKey = getSessionRoomsKey(sessionId);
        String memberSessionsKey = getMemberSessionsKey(memberId);

        // 1. 세션별 구독 제거
        redisTemplate.opsForSet().remove(sessionsKey, sessionId);

        // 2. 세션이 구독 중인 방 목록에서도 제거
        redisTemplate.opsForSet().remove(sessionRoomsKey, String.valueOf(roomId));

        // 3. 이 회원의 다른 세션이 남아있는지 확인 (매핑 삭제 전에 확인)
        boolean hasOtherSessions = checkOtherSessionsForMember(roomId, memberId);

        // 4. 모든 세션이 해제되었으면 members Set에서도 제거
        if (!hasOtherSessions) {
            redisTemplate.opsForSet().remove(membersKey, String.valueOf(memberId));
        }
        
        // 주의: memberSessionsKey(회원->세션)에서는 여기서 바로 삭제하지 않음.
        // 왜냐하면 해당 세션이 '다른 방'에는 여전히 구독 중일 수 있기 때문.
        // 이 키는 cleanUpSession에서 세션 자체가 죽을 때 삭제하거나, TTL로 관리됨.
    }
    
    /**
     * 특정 회원의 모든 세션을 해당 방에서 강제 구독 해제 (강퇴, 방 나가기 용)
     */
    public void removeSubscribersByMemberId(Long roomId, Long memberId) {
        String memberSessionsKey = getMemberSessionsKey(memberId);
        
        // 회원의 모든 활성 세션 조회
        Set<String> sessionIds = redisTemplate.opsForSet().members(memberSessionsKey);
        
        if (sessionIds != null && !sessionIds.isEmpty()) {
            for (String sessionId : sessionIds) {
                // 각 세션에 대해 구독 해제 수행
                removeSubscriber(roomId, memberId, sessionId);
            }
        }
    }

    /**
     * 세션 종료 시 해당 세션과 관련된 모든 구독 정보 정리 (유령 구독자 방지 핵심)
     * 메모리 상태와 무관하게 Redis 데이터만으로 정리 가능
     */
    public void cleanUpSession(String sessionId) {
        String mappingKey = getSessionMappingKey(sessionId);
        String sessionRoomsKey = getSessionRoomsKey(sessionId);

        // 1. 회원 ID 조회
        String memberIdStr = redisTemplate.opsForValue().get(mappingKey);
        if (memberIdStr == null) {
            return; // 이미 만료되었거나 없는 세션
        }
        Long memberId = Long.parseLong(memberIdStr);

        // 2. 이 세션이 구독 중이던 모든 방 조회
        Set<String> roomIds = redisTemplate.opsForSet().members(sessionRoomsKey);

        if (roomIds != null && !roomIds.isEmpty()) {
            for (String roomIdStr : roomIds) {
                try {
                    Long roomId = Long.parseLong(roomIdStr);
                    // 각 방에서 구독 해제 처리
                    removeSubscriber(roomId, memberId, sessionId);
                } catch (NumberFormatException e) {
                    log.error("잘못된 Room ID 형식: {}", roomIdStr);
                }
            }
        }
        
        // 3. 회원 -> 세션 목록에서도 제거
        String memberSessionsKey = getMemberSessionsKey(memberId);
        redisTemplate.opsForSet().remove(memberSessionsKey, sessionId);

        // 4. 세션 관련 메타 데이터 최종 삭제
        redisTemplate.delete(mappingKey);
        redisTemplate.delete(sessionRoomsKey);

        log.debug("세션 정리 완료 - sessionId: {}, memberId: {}", sessionId, memberId);
    }

    // 현재 구독 중인지 확인 (빠른 조회용 - O(1))
    public boolean isSubscribed(Long roomId, Long memberId) {
        String membersKey = getMembersKey(roomId);
        Boolean isMember = redisTemplate.opsForSet().isMember(membersKey, String.valueOf(memberId));
        return Boolean.TRUE.equals(isMember);
    }

    // 현재 구독 중인 모든 사용자 조회 (members Set 기반)
    public Set<String> getSubscribers(Long roomId) {
        String membersKey = getMembersKey(roomId);
        return redisTemplate.opsForSet().members(membersKey);
    }

    // 특정 회원의 다른 세션이 남아있는지 확인
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
}
