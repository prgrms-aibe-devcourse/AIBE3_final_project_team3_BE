package triplestar.mixchat.global.websocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import triplestar.mixchat.domain.chat.chat.dto.ReadStatusUpdateEvent;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.service.ChatMemberService;
import triplestar.mixchat.global.cache.ChatSubscriberCacheService;
import triplestar.mixchat.global.security.CustomUserDetails;

// WebSocket 구독/구독 해제 이벤트 감지 및 자동 읽음 처리
@Component
@Slf4j
public class WebSocketEventListener {

    private final ChatSubscriberCacheService subscriberCacheService;
    private final ChatMemberService chatMemberService;
    private final SimpMessageSendingOperations messagingTemplate;

    // 세션별 구독 중인 방 목록 추적 (KEYS 명령어 사용 방지)
    private final ConcurrentHashMap<String, SessionSubscription> sessionSubscriptions = new ConcurrentHashMap<>();

    // subscriptionId와 roomId 매핑 (구독 해제 시 사용)
    private final ConcurrentHashMap<String, RoomSubscriptionInfo> subscriptionIdToRoomInfo = new ConcurrentHashMap<>();

    private static final Pattern ROOM_DESTINATION_PATTERN =
            Pattern.compile("^/topic/(direct|group|ai)/rooms/(\\d+)");

    public WebSocketEventListener(ChatSubscriberCacheService subscriberCacheService,
                                   ChatMemberService chatMemberService,
                                   SimpMessageSendingOperations messagingTemplate) {
        this.subscriberCacheService = subscriberCacheService;
        this.chatMemberService = chatMemberService;
        this.messagingTemplate = messagingTemplate;
    }

    // 세션별 구독 정보 저장용 내부 클래스(redis keys 사용 방지)
    private static class SessionSubscription {
        private final Long memberId;
        private final Set<Long> roomIds = ConcurrentHashMap.newKeySet();

        public SessionSubscription(Long memberId) {
            this.memberId = memberId;
        }

        public void addRoom(Long roomId) {
            roomIds.add(roomId);
        }

        public void removeRoom(Long roomId) {
            roomIds.remove(roomId);
        }

        public Set<Long> getRoomIds() {
            return roomIds;
        }

        public Long getMemberId() {
            return memberId;
        }
    }

    // subscriptionId별 방 정보 저장용 내부 클래스
    private static class RoomSubscriptionInfo {
        private final Long roomId;
        private final Long memberId;
        private final String sessionId;

        public RoomSubscriptionInfo(Long roomId, Long memberId, String sessionId) {
            this.roomId = roomId;
            this.memberId = memberId;
            this.sessionId = sessionId;
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
        ChatMessage.chatRoomType chatRoomType = ChatMessage.chatRoomType.valueOf(typeString);

        // AI 채팅방은 읽음 처리 제외
        if (chatRoomType == ChatMessage.chatRoomType.AI) {
            return;
        }

        // Redis에 구독자 추가
        subscriberCacheService.addSubscriber(roomId, memberId);

        // 세션별 구독 방 추적 (disconnect 시 사용)
        sessionSubscriptions.computeIfAbsent(sessionId, k -> new SessionSubscription(memberId))
                .addRoom(roomId);

        // subscriptionId와 roomId 매핑 저장 (unsubscribe 시 사용)
        subscriptionIdToRoomInfo.put(subscriptionId, new RoomSubscriptionInfo(roomId, memberId, sessionId));

        // 채팅방 입장 시 해당 방의 모든 메시지를 읽음 처리
        Long readSequence = chatMemberService.markAsReadOnEnter(memberId, roomId, chatRoomType);

        // 실제로 새로 읽은 메시지가 있을 때만 읽음 이벤트를 브로드캐스트
        // readSequence가 null이면 이미 모든 메시지를 읽은 상태 (새로고침 등)
        if (readSequence != null && readSequence > 0) {
            ReadStatusUpdateEvent readEvent = ReadStatusUpdateEvent.of(memberId, readSequence);
            String broadcastDestination = "/topic/" + typeString.toLowerCase() + "/rooms/" + roomId;
            messagingTemplate.convertAndSend(broadcastDestination, readEvent);
            log.info("Broadcasted read event: memberId={}, roomId={}, readSequence={}",
                    memberId, roomId, readSequence);
        } else {
            log.debug("No new messages to mark as read: memberId={}, roomId={}", memberId, roomId);
        }

        log.info("User subscribed and marked as read: memberId={}, roomId={}, type={}, sessionId={}",
                memberId, roomId, chatRoomType, sessionId);
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
            return;
        }

        Long roomId = roomInfo.getRoomId();
        Long memberId = roomInfo.getMemberId();
        String sessionId = roomInfo.getSessionId();

        // Redis에서 구독자 제거
        subscriberCacheService.removeSubscriber(roomId, memberId);

        // 세션별 구독 방 목록에서도 제거
        SessionSubscription sessionSubscription = sessionSubscriptions.get(sessionId);
        if (sessionSubscription != null) {
            sessionSubscription.removeRoom(roomId);
        }

        log.info("User unsubscribed: memberId={}, roomId={}, sessionId={}", memberId, roomId, sessionId);
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
            return;
        }

        Long memberId = subscription.getMemberId();
        Set<Long> roomIds = subscription.getRoomIds();

        // 실제 구독한 방만 Redis에서 제거
        for (Long roomId : roomIds) {
            subscriberCacheService.removeSubscriber(roomId, memberId);
        }

        log.info("User disconnected: memberId={}, sessionId={}, removed from {} rooms",
                memberId, sessionId, roomIds.size());
    }
}
