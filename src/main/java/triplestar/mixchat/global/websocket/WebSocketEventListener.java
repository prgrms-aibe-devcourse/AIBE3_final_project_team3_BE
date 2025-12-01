package triplestar.mixchat.global.websocket;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.MessageUnreadCountResp;
import triplestar.mixchat.domain.chat.chat.dto.SubscriberCountUpdateResp;
import triplestar.mixchat.domain.chat.chat.dto.UnreadCountUpdateEvent;
import triplestar.mixchat.domain.chat.chat.service.ChatMemberService;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.global.cache.ChatSubscriberCacheService;
import triplestar.mixchat.global.security.CustomUserDetails;

// WebSocket 구독/구독 해제 이벤트 감지 및 자동 읽음 처리
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final ChatSubscriberCacheService subscriberCacheService;
    private final ChatMemberService chatMemberService;
    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations messagingTemplate;

    // 세션별 구독 중인 방 목록 추적 (KEYS 명령어 사용 방지)
    private final ConcurrentHashMap<String, SessionSubscription> sessionSubscriptions = new ConcurrentHashMap<>();

    // subscriptionId와 roomId 매핑 (구독 해제 시 사용)
    private final ConcurrentHashMap<String, RoomSubscriptionInfo> subscriptionIdToRoomInfo = new ConcurrentHashMap<>();

    private static final Pattern ROOM_DESTINATION_PATTERN =
            Pattern.compile("^/topic/(direct|group|ai)/rooms/(\\d+)");

    // 세션별 구독 정보 저장용 내부 클래스(redis keys 사용 방지)
    private static class SessionSubscription {
        private final Long memberId;
        private final Set<String> subscriptionIds = ConcurrentHashMap.newKeySet(); // disconnect 시 subscriptionIdToRoomInfo 정리용
        private final ConcurrentHashMap<Long, ChatRoomType> roomTypeMap = new ConcurrentHashMap<>(); // roomId -> chatRoomType

        public SessionSubscription(Long memberId) {
            this.memberId = memberId;
        }

        public void addRoom(Long roomId, String subscriptionId, ChatRoomType chatRoomType) {
            subscriptionIds.add(subscriptionId);
            roomTypeMap.put(roomId, chatRoomType);
        }

        public void removeRoom(Long roomId, String subscriptionId) {
            subscriptionIds.remove(subscriptionId);
            roomTypeMap.remove(roomId);
        }

        public Set<String> getSubscriptionIds() {
            return subscriptionIds;
        }

        public Long getMemberId() {
            return memberId;
        }

        public ConcurrentHashMap<Long, ChatRoomType> getRoomTypeMap() {
            return roomTypeMap;
        }
    }

    // subscriptionId별 방 정보 저장용 내부 클래스
    private static class RoomSubscriptionInfo {
        private final Long roomId;
        private final Long memberId;
        private final String sessionId;
        private final ChatRoomType chatRoomType;

        public RoomSubscriptionInfo(Long roomId, Long memberId, String sessionId, ChatRoomType chatRoomType) {
            this.roomId = roomId;
            this.memberId = memberId;
            this.sessionId = sessionId;
            this.chatRoomType = chatRoomType;
        }

        public Long getRoomId() {
            return roomId;
        }

        public Long getMemberId() {
            return memberId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public ChatRoomType getChatRoomType() {
            return chatRoomType;
        }
    }

    // 채팅방 구독 시작 - 자동 읽음 처리 및 읽음 이벤트 브로드캐스트
    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();

        if (destination == null || sessionId == null || subscriptionId == null) {
            return;
        }

        Matcher matcher = ROOM_DESTINATION_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return;
        }

        Authentication authentication = (Authentication) accessor.getUser();
        if (authentication == null) {
            return;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getId();

        String typeString = matcher.group(1).toUpperCase();
        Long roomId = Long.parseLong(matcher.group(2));
        ChatRoomType chatRoomType = ChatRoomType.valueOf(typeString);

        // AI 채팅방은 읽음 처리 및 구독자 추적이 불필요 (1:1 AI 대화)
        if (chatRoomType == ChatRoomType.AI) {
            return;
        }

        try {
            // 1. 먼저 읽음 처리 (DB 작업 - 실패 가능성 높음)
            // 실패 시 Redis/메모리에 유령 데이터가 남지 않도록 가장 먼저 실행
            Long readSequence = chatMemberService.markAsReadOnEnter(memberId, roomId, chatRoomType);

            // 2. 성공하면 Redis에 구독자 추가 (실패 가능성 낮음)
            subscriberCacheService.addSubscriber(roomId, memberId, sessionId);

            // 3. 세션별 구독 방 추적 (disconnect 시 사용)
            sessionSubscriptions.computeIfAbsent(sessionId, k -> new SessionSubscription(memberId))
                    .addRoom(roomId, subscriptionId, chatRoomType);

            // 4. subscriptionId와 roomId 매핑 저장 (unsubscribe 시 사용)
            subscriptionIdToRoomInfo.put(subscriptionId, new RoomSubscriptionInfo(roomId, memberId, sessionId, chatRoomType));

            // 5. 실제로 새로 읽은 메시지가 있을 때만 unreadCount 업데이트 이벤트를 브로드캐스트
            // readSequence가 null이면 이미 모든 메시지를 읽은 상태 (새로고침 등)
            if (readSequence != null && readSequence > 0) {
                // 영향받은 메시지들의 최신 unreadCount 계산
                List<MessageUnreadCountResp> updates = chatMessageService.getUnreadCountUpdates(roomId, chatRoomType, readSequence);

                if (!updates.isEmpty()) {
                    UnreadCountUpdateEvent updateEvent = UnreadCountUpdateEvent.from(updates);
                    String broadcastDestination = "/topic/" + typeString.toLowerCase() + "/rooms/" + roomId;
                    messagingTemplate.convertAndSend(broadcastDestination, updateEvent);
                }
            }

            // 6. 구독자 수 변경 브로드캐스트
            broadcastSubscriberCount(roomId, chatRoomType);

        } catch (IllegalArgumentException | AccessDeniedException e) {
            // 채팅방 권한 또는 데이터 검증 실패 시 로그만 남기고 클라이언트는 구독 실패로 처리
            // Redis/메모리에 유령 데이터가 남지 않음 (원자성 보장)
            log.error("채팅방 구독 처리 실패 - memberId: {}, roomId: {}, type: {}, error: {}",
                    memberId, roomId, chatRoomType, e.getMessage());
            // 예외를 다시 던지지 않음 (StompHandler에서 이미 멤버십 검증 완료)
        } catch (Exception e) {
            // 예상치 못한 예외는 별도 처리 (모니터링 필요)
            log.error("채팅방 구독 처리 중 예상치 못한 오류 - memberId: {}, roomId: {}, type: {}",
                    memberId, roomId, chatRoomType, e);
        }
    }

    // 채팅방 구독 해제
    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String subscriptionId = accessor.getSubscriptionId();

        if (subscriptionId == null) {
            return;
        }

        // subscriptionId로 방 정보 조회 및 제거
        RoomSubscriptionInfo roomInfo = subscriptionIdToRoomInfo.remove(subscriptionId);
        if (roomInfo == null) {
            log.warn("구독 해제 실패 - 구독 정보 없음: subscriptionId={}", subscriptionId);
            return;
        }

        Long roomId = roomInfo.getRoomId();
        Long memberId = roomInfo.getMemberId();
        String sessionId = roomInfo.getSessionId();

        // Redis에서 구독자 제거 (세션 ID 포함)
        subscriberCacheService.removeSubscriber(roomId, memberId, sessionId);

        // 세션별 구독 방 목록에서도 제거
        SessionSubscription sessionSubscription = sessionSubscriptions.get(sessionId);
        if (sessionSubscription != null) {
            sessionSubscription.removeRoom(roomId, subscriptionId);
        }

        // 구독자 수 변경 브로드캐스트
        ChatRoomType chatRoomType = roomInfo.getChatRoomType();
        broadcastSubscriberCount(roomId, chatRoomType);
    }

    // WebSocket 세션 종료 - 해당 세션이 구독한 방만 제거 (KEYS 사용 방지)
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId == null) {
            return;
        }

        // 세션별 구독 정보 조회 및 제거
        SessionSubscription subscription = sessionSubscriptions.remove(sessionId);
        if (subscription == null) {
            log.warn("WebSocket 연결 종료 - 세션 정보 없음: sessionId={}", sessionId);
            return;
        }

        Long memberId = subscription.getMemberId();
        ConcurrentHashMap<Long, ChatRoomType> roomTypeMap = subscription.getRoomTypeMap();
        Set<String> subscriptionIds = subscription.getSubscriptionIds();

        // 실제 구독한 방만 Redis에서 제거 및 구독자 수 브로드캐스트
        for (Map.Entry<Long, ChatRoomType> entry : roomTypeMap.entrySet()) {
            Long roomId = entry.getKey();
            ChatRoomType chatRoomType = entry.getValue();

            // AI 채팅방은 읽음 처리 및 구독자 추적이 불필요 (1:1 AI 대화)
            if (chatRoomType == ChatRoomType.AI) {
                continue;
            }

            subscriberCacheService.removeSubscriber(roomId, memberId, sessionId);

            // 구독자 수 변경 브로드캐스트
            broadcastSubscriberCount(roomId, chatRoomType);
        }

        // [중요] subscriptionIdToRoomInfo 맵에서도 제거 (메모리 누수 방지)
        for (String subId : subscriptionIds) {
            subscriptionIdToRoomInfo.remove(subId);
        }
    }

    // 구독자 수 변경 브로드캐스트 헬퍼 메서드
    private void broadcastSubscriberCount(Long roomId, ChatRoomType chatRoomType) {
        // AI 채팅방은 읽음 처리 및 구독자 추적이 불필요 (1:1 AI 대화)
        if (chatRoomType == ChatRoomType.AI) {
            return;
        }

        // 현재 구독자 수 조회
        int subscriberCount = chatMemberService.getSubscriberCount(roomId);

        // 전체 멤버 수 조회
        int totalMemberCount = chatMemberService.getTotalMemberCount(roomId, chatRoomType);

        // 브로드캐스트
        SubscriberCountUpdateResp resp = SubscriberCountUpdateResp.of(subscriberCount, totalMemberCount);
        String destination = "/topic/" + chatRoomType.name().toLowerCase() + "/rooms/" + roomId;
        messagingTemplate.convertAndSend(destination, resp);
    }
}