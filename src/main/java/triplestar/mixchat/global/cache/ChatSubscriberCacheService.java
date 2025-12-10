package triplestar.mixchat.global.cache;

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

    private String getSessionsKey(Long roomId) {
        return SESSIONS_KEY_PREFIX + roomId + SESSIONS_KEY_SUFFIX;
    }

    private String getMembersKey(Long roomId) {
        return SESSIONS_KEY_PREFIX + roomId + MEMBERS_KEY_SUFFIX;
    }

    private String getSessionMappingKey(String sessionId) {
        return SESSION_MEMBER_MAPPING_PREFIX + sessionId;
    }

//    // 파이프라인 없이 하면 얼마나 걸리나 테스트용
//    public void addSubscriber(Long roomId, Long memberId, String sessionId) {
//        String sessionsKey = getSessionsKey(roomId);
//        String membersKey = getMembersKey(roomId);
//        String mappingKey = getSessionMappingKey(sessionId);
//
//        // 1. 세션별 구독 추가
//        redisTemplate.opsForSet().add(sessionsKey, sessionId);
//        redisTemplate.expire(sessionsKey, SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);
//
//        // 2. 세션-멤버 매핑 저장
//        redisTemplate.opsForValue().set(mappingKey, String.valueOf(memberId), SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);
//
//        // 3. 멤버별 집계에 추가 (Set이므로 중복 자동 제거)
//        redisTemplate.opsForSet().add(membersKey, String.valueOf(memberId));
//        redisTemplate.expire(membersKey, SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);
//    }

    // 채팅방 구독 시작
    public void addSubscriber(Long roomId, Long memberId, String sessionId) {
        String sessionsKey = getSessionsKey(roomId);
        String membersKey = getMembersKey(roomId);
        String mappingKey = getSessionMappingKey(sessionId);

        // Redis 왕복을 줄이기 위해 세션/멤버 추가와 TTL 설정을 파이프라인 처리
        redisTemplate.executePipelined(new SessionCallback<Void>() {
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> Void execute(RedisOperations<K, V> operations) throws DataAccessException {
                RedisOperations<String, String> redisOps = (RedisOperations<String, String>) operations;

                redisOps.opsForSet().add(sessionsKey, sessionId);
                redisOps.expire(sessionsKey, SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);

                redisOps.opsForValue().set(mappingKey, String.valueOf(memberId),
                        SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);

                redisOps.opsForSet().add(membersKey, String.valueOf(memberId));
                redisOps.expire(membersKey, SUBSCRIBER_TTL_SECONDS, TimeUnit.SECONDS);

                return null;
            }
        });
    }

    // 채팅방 구독 해제
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

    // 현재 구독 중인 모든 세션 조회
    public Set<String> getSubscribedSessions(Long roomId) {
        String sessionsKey = getSessionsKey(roomId);
        return redisTemplate.opsForSet().members(sessionsKey);
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

    // 특정 세션의 회원 ID 조회
    public Long getMemberIdBySessionId(String sessionId) {
        String mappingKey = getSessionMappingKey(sessionId);
        String memberIdStr = redisTemplate.opsForValue().get(mappingKey);
        return memberIdStr != null ? Long.parseLong(memberIdStr) : null;
    }

    // 특정 회원의 모든 세션 ID 조회
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

    /**
     * 채팅방의 모든 구독자 정보 삭제 (관리자 강제 폐쇄 시 사용)
     * - 모든 세션-멤버 매핑 삭제
     * - 세션 Set 삭제
     * - 멤버 Set 삭제
     *
     * TODO: AdminChatRoomService에서 다음과 같이 사용:
     *
     * 1. AdminChatRoomService에 필드 추가 및 생성자 주입:
     *    private final ChatSubscriberCacheService subscriberCacheService;
     *
     * 2. forceCloseRoom 메서드 내에서 다음 순서로 호출:
     *    public void forceCloseRoom(Long roomId, String reason) {
     *        // ... 관리자 권한 검증 로직 ...
     *
     *        // 1. DB에서 채팅방 상태 변경 (CLOSED 등)
     *        chatRoomRepository.updateStatus(roomId, ChatRoomStatus.CLOSED);
     *
     *        // 2. Redis에서 모든 구독자 정보 삭제 (중요: DB 변경 후 수행)
     *        subscriberCacheService.removeAllSubscribers(roomId);
     *
     *        // 3. WebSocket으로 강제 퇴장 이벤트 브로드캐스트
     *        //    (removeAllSubscribers 호출 전에 브로드캐스트해야 구독자가 메시지 수신 가능)
     *        ForceCloseEvent event = ForceCloseEvent.of(roomId, reason);
     *        messagingTemplate.convertAndSend("/topic/group/rooms/" + roomId, event);
     *
     *        // 4. 시스템 메시지 저장 (선택 사항)
     *        chatMessageService.createSystemMessage(roomId, "방이 관리자에 의해 폐쇄되었습니다: " + reason);
     *
     *        log.info("관리자에 의해 채팅방 강제 폐쇄 - roomId: {}, reason: {}", roomId, reason);
     *    }
     *
     * 3. 브로드캐스트는 구독자 삭제 **전에** 수행해야 메시지 수신 가능
     *
     * @param roomId 채팅방 ID
     */
    public void removeAllSubscribers(Long roomId) {
        String sessionsKey = getSessionsKey(roomId);
        String membersKey = getMembersKey(roomId);

        // 1. 모든 세션 조회
        Set<String> allSessions = redisTemplate.opsForSet().members(sessionsKey);

        if (allSessions != null && !allSessions.isEmpty()) {
            // 2. 각 세션의 매핑 정보 삭제
            for (String sessionId : allSessions) {
                String mappingKey = getSessionMappingKey(sessionId);
                redisTemplate.delete(mappingKey);
            }
            log.info("채팅방 구독자 정리 완료 - roomId: {}, 삭제된 세션 수: {}", roomId, allSessions.size());
        }

        // 3. 세션 Set 삭제
        redisTemplate.delete(sessionsKey);

        // 4. 멤버 Set 삭제
        redisTemplate.delete(membersKey);
    }
}
