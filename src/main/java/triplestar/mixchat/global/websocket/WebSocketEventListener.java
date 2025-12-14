package triplestar.mixchat.global.websocket;

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
import triplestar.mixchat.domain.chat.chat.service.ChatMemberService;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.global.cache.ChatSubscriberCacheService;
import triplestar.mixchat.global.security.CustomUserDetails;

/**
 * WebSocket 구독/해제/종료 이벤트를 처리하며, 단순 구독 시에는 읽음 처리하지 않는다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatSubscriberCacheService subscriberCacheService;
    private final ChatMemberService chatMemberService;
    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations messagingTemplate;

    private final ConcurrentHashMap<String, SessionSubscription> sessionSubscriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RoomSubscriptionInfo> subscriptionIdToRoomInfo = new ConcurrentHashMap<>();

    private static final Pattern ROOM_DESTINATION_PATTERN =
            Pattern.compile("^/topic/(direct|group|ai)\\.rooms\\.(\\d+)");

    private static class SessionSubscription {
        private final Long memberId;
        private final Set<String> subscriptionIds = ConcurrentHashMap.newKeySet();
        private final ConcurrentHashMap<Long, ChatRoomType> roomTypeMap = new ConcurrentHashMap<>();

        SessionSubscription(Long memberId) {
            this.memberId = memberId;
        }

        void addRoom(Long roomId, String subscriptionId, ChatRoomType chatRoomType) {
            subscriptionIds.add(subscriptionId);
            roomTypeMap.put(roomId, chatRoomType);
        }

        void removeRoom(Long roomId, String subscriptionId) {
            subscriptionIds.remove(subscriptionId);
            roomTypeMap.remove(roomId);
        }

        Long getMemberId() {
            return memberId;
        }

        ConcurrentHashMap<Long, ChatRoomType> getRoomTypeMap() {
            return roomTypeMap;
        }

        Set<String> getSubscriptionIds() {
            return subscriptionIds;
        }
    }

    private static class RoomSubscriptionInfo {
        private final Long roomId;
        private final Long memberId;
        private final String sessionId;
        private final ChatRoomType chatRoomType;

        RoomSubscriptionInfo(Long roomId, Long memberId, String sessionId, ChatRoomType chatRoomType) {
            this.roomId = roomId;
            this.memberId = memberId;
            this.sessionId = sessionId;
            this.chatRoomType = chatRoomType;
        }

        Long getRoomId() {
            return roomId;
        }

        Long getMemberId() {
            return memberId;
        }

        String getSessionId() {
            return sessionId;
        }

        ChatRoomType getChatRoomType() {
            return chatRoomType;
        }
    }

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

        if (chatRoomType == ChatRoomType.AI) {
            return;
        }

        try {
            // 멤버 검증만 수행 (읽음 처리는 실제 입장/메시지 조회 시점에 수행)
            chatMemberService.verifyUserIsMemberOfRoom(memberId, roomId, chatRoomType);

            subscriberCacheService.addSubscriber(roomId, memberId, sessionId);

            sessionSubscriptions.computeIfAbsent(sessionId, k -> new SessionSubscription(memberId))
                    .addRoom(roomId, subscriptionId, chatRoomType);

            subscriptionIdToRoomInfo.put(subscriptionId, new RoomSubscriptionInfo(roomId, memberId, sessionId, chatRoomType));

            chatMemberService.broadcastSubscriberCount(roomId, chatRoomType);

        } catch (IllegalArgumentException | AccessDeniedException e) {
            log.error("채팅방 구독 처리 실패 - memberId: {}, roomId: {}, type: {}, error: {}",
                    memberId, roomId, chatRoomType, e.getMessage());
        } catch (Exception e) {
            log.error("채팅방 구독 처리 중 예기치 못한 오류 - memberId: {}, roomId: {}, type: {}",
                    memberId, roomId, chatRoomType, e);
        }
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String subscriptionId = accessor.getSubscriptionId();

        if (subscriptionId == null) {
            return;
        }

        RoomSubscriptionInfo roomInfo = subscriptionIdToRoomInfo.remove(subscriptionId);
        if (roomInfo == null) {
            log.warn("구독 해제 실패 - 구독 정보 없음: subscriptionId={}", subscriptionId);
            return;
        }

        Long roomId = roomInfo.getRoomId();
        Long memberId = roomInfo.getMemberId();
        String sessionId = roomInfo.getSessionId();

        subscriberCacheService.removeSubscriber(roomId, memberId, sessionId);

        SessionSubscription sessionSubscription = sessionSubscriptions.get(sessionId);
        if (sessionSubscription != null) {
            sessionSubscription.removeRoom(roomId, subscriptionId);
        }

        ChatRoomType chatRoomType = roomInfo.getChatRoomType();
        chatMemberService.broadcastSubscriberCount(roomId, chatRoomType);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId == null) {
            return;
        }

        SessionSubscription subscription = sessionSubscriptions.remove(sessionId);
        if (subscription == null) {
            log.warn("WebSocket 연결 종료 - 세션 정보 없음: sessionId={}", sessionId);
            return;
        }

        Long memberId = subscription.getMemberId();
        ConcurrentHashMap<Long, ChatRoomType> roomTypeMap = subscription.getRoomTypeMap();
        Set<String> subscriptionIds = subscription.getSubscriptionIds();

        for (Map.Entry<Long, ChatRoomType> entry : roomTypeMap.entrySet()) {
            Long roomId = entry.getKey();
            ChatRoomType chatRoomType = entry.getValue();

            if (chatRoomType == ChatRoomType.AI) {
                continue;
            }

            subscriberCacheService.removeSubscriber(roomId, memberId, sessionId);
            chatMemberService.broadcastSubscriberCount(roomId, chatRoomType);
        }

        for (String subId : subscriptionIds) {
            subscriptionIdToRoomInfo.remove(subId);
        }
    }
}
