package triplestar.mixchat.global.websocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import java.util.List;
import triplestar.mixchat.domain.chat.chat.dto.MessageUnreadCountDto;
import triplestar.mixchat.domain.chat.chat.dto.ReadStatusUpdateEvent;
import triplestar.mixchat.domain.chat.chat.dto.SubscriberCountUpdateResp;
import triplestar.mixchat.domain.chat.chat.dto.UnreadCountUpdateEventDto;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
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
        private final Set<Long> roomIds = ConcurrentHashMap.newKeySet();
        private final Set<String> subscriptionIds = ConcurrentHashMap.newKeySet(); // disconnect 시 subscriptionIdToRoomInfo 정리용
        private final ConcurrentHashMap<Long, ChatMessage.chatRoomType> roomTypeMap = new ConcurrentHashMap<>(); // roomId -> chatRoomType

        public SessionSubscription(Long memberId) {
            this.memberId = memberId;
        }

        public void addRoom(Long roomId, String subscriptionId, ChatMessage.chatRoomType chatRoomType) {
            roomIds.add(roomId);
            subscriptionIds.add(subscriptionId);
            roomTypeMap.put(roomId, chatRoomType);
        }

        public void removeRoom(Long roomId, String subscriptionId) {
            roomIds.remove(roomId);
            subscriptionIds.remove(subscriptionId);
            roomTypeMap.remove(roomId);
        }

        public Set<Long> getRoomIds() {
            return roomIds;
        }

        public Set<String> getSubscriptionIds() {
            return subscriptionIds;
        }

        public Long getMemberId() {
            return memberId;
        }

        public ChatMessage.chatRoomType getRoomType(Long roomId) {
            return roomTypeMap.get(roomId);
        }
    }

    // subscriptionId별 방 정보 저장용 내부 클래스
    private static class RoomSubscriptionInfo {
        private final Long roomId;
        private final Long memberId;
        private final String sessionId;
        private final ChatMessage.chatRoomType chatRoomType;

        public RoomSubscriptionInfo(Long roomId, Long memberId, String sessionId, ChatMessage.chatRoomType chatRoomType) {
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

        public ChatMessage.chatRoomType getChatRoomType() {
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
        ChatMessage.chatRoomType chatRoomType = ChatMessage.chatRoomType.valueOf(typeString);

        // AI 채팅방은 읽음 처리 제외
        if (chatRoomType == ChatMessage.chatRoomType.AI) {
            return;
        }

        // Redis에 구독자 추가 (세션 ID 포함)
        subscriberCacheService.addSubscriber(roomId, memberId, sessionId);
        log.info("[Subscribe] Added to Redis: roomId={}, memberId={}, sessionId={}, subscriptionId={}",
                roomId, memberId, sessionId, subscriptionId);

        // 세션별 구독 방 추적 (disconnect 시 사용)
        sessionSubscriptions.computeIfAbsent(sessionId, k -> new SessionSubscription(memberId))
                .addRoom(roomId, subscriptionId, chatRoomType);

        // subscriptionId와 roomId 매핑 저장 (unsubscribe 시 사용)
        subscriptionIdToRoomInfo.put(subscriptionId, new RoomSubscriptionInfo(roomId, memberId, sessionId, chatRoomType));

        // 채팅방 입장 시 해당 방의 모든 메시지를 읽음 처리
        Long readSequence = chatMemberService.markAsReadOnEnter(memberId, roomId, chatRoomType);

        // 실제로 새로 읽은 메시지가 있을 때만 unreadCount 업데이트 이벤트를 브로드캐스트
        // readSequence가 null이면 이미 모든 메시지를 읽은 상태 (새로고침 등)
        if (readSequence != null && readSequence > 0) {
            // 영향받은 메시지들의 최신 unreadCount 계산
            List<MessageUnreadCountDto> updates = chatMessageService.getUnreadCountUpdates(roomId, chatRoomType, readSequence);

            if (!updates.isEmpty()) {
                UnreadCountUpdateEventDto updateEvent = UnreadCountUpdateEventDto.from(updates);
                String broadcastDestination = "/topic/" + typeString.toLowerCase() + "/rooms/" + roomId;

                log.info("🔔 [UNREAD COUNT UPDATE] Broadcasting to ALL subscribers: destination={}, updatedCount={}, readerId={}, readSequence={}",
                        broadcastDestination, updates.size(), memberId, readSequence);

                messagingTemplate.convertAndSend(broadcastDestination, updateEvent);

                log.info("✅ [UNREAD COUNT UPDATE] Broadcast completed: destination={}, {} messages updated",
                        broadcastDestination, updates.size());
            } else {
                log.info("⏭️ [UNREAD COUNT UPDATE] No messages to update for roomId={}", roomId);
            }
        } else {
            log.info("⏭️ [UNREAD COUNT UPDATE] Skipped (already read all): memberId={}, roomId={}", memberId, roomId);
        }

        // 구독자 수 변경 브로드캐스트
        broadcastSubscriberCount(roomId, chatRoomType);

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
            log.warn("[Unsubscribe] No room info found for subscriptionId={}", subscriptionId);
            return;
        }

        Long roomId = roomInfo.getRoomId();
        Long memberId = roomInfo.getMemberId();
        String sessionId = roomInfo.getSessionId();

        log.info("[Unsubscribe] Removing from Redis: roomId={}, memberId={}, sessionId={}, subscriptionId={}",
                roomId, memberId, sessionId, subscriptionId);

        // Redis에서 구독자 제거 (세션 ID 포함)
        subscriberCacheService.removeSubscriber(roomId, memberId, sessionId);

        // 세션별 구독 방 목록에서도 제거
        SessionSubscription sessionSubscription = sessionSubscriptions.get(sessionId);
        if (sessionSubscription != null) {
            sessionSubscription.removeRoom(roomId, subscriptionId);
        }

        // 구독자 수 변경 브로드캐스트
        ChatMessage.chatRoomType chatRoomType = roomInfo.getChatRoomType();
        broadcastSubscriberCount(roomId, chatRoomType);

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
            log.warn("[Disconnect] No subscription info found for sessionId={}", sessionId);
            return;
        }

        Long memberId = subscription.getMemberId();
        Set<Long> roomIds = subscription.getRoomIds();
        Set<String> subscriptionIds = subscription.getSubscriptionIds();

        log.info("[Disconnect] Cleaning up session: sessionId={}, memberId={}, roomCount={}, subscriptionCount={}",
                sessionId, memberId, roomIds.size(), subscriptionIds.size());

        // 실제 구독한 방만 Redis에서 제거 및 구독자 수 브로드캐스트
        // SessionSubscription에서 직접 roomId와 chatRoomType을 가져와서 처리 (subscriptionIdToRoomInfo에 의존하지 않음)
        for (Long roomId : roomIds) {
            ChatMessage.chatRoomType chatRoomType = subscription.getRoomType(roomId);
            if (chatRoomType == null || chatRoomType == ChatMessage.chatRoomType.AI) {
                continue; // AI 채팅방은 제외
            }

            log.info("[Disconnect] Removing from Redis: roomId={}, memberId={}, sessionId={}",
                    roomId, memberId, sessionId);

            subscriberCacheService.removeSubscriber(roomId, memberId, sessionId);

            // 구독자 수 변경 브로드캐스트
            broadcastSubscriberCount(roomId, chatRoomType);
        }

        // [중요] subscriptionIdToRoomInfo 맵에서도 제거 (메모리 누수 방지)
        for (String subId : subscriptionIds) {
            subscriptionIdToRoomInfo.remove(subId);
        }

        log.info("User disconnected: memberId={}, sessionId={}, removed from {} rooms, cleaned {} subscriptions",
                memberId, sessionId, roomIds.size(), subscriptionIds.size());
    }

    // 구독자 수 변경 브로드캐스트 헬퍼 메서드
    private void broadcastSubscriberCount(Long roomId, ChatMessage.chatRoomType chatRoomType) {
        // AI 채팅방은 제외
        if (chatRoomType == ChatMessage.chatRoomType.AI) {
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

        log.info("Broadcasted subscriber count: roomId={}, type={}, subscriberCount={}, totalMemberCount={}",
                roomId, chatRoomType, subscriberCount, totalMemberCount);
    }
}
