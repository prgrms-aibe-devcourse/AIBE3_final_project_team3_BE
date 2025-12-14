package triplestar.mixchat.domain.chat.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.dto.RoomLastMessageUpdateResp;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;

import triplestar.mixchat.domain.chat.chat.dto.MessageUnreadCountResp;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatNotificationService {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    // 읽음 카운트 업데이트 전송
    public void sendUnreadCountUpdate(Long roomId, ChatRoomType type, List<MessageUnreadCountResp> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        // 프론트엔드 구분을 위해 래퍼 객체나 type 필드가 있는 것이 좋으나,
        // 현재 구조상 List를 그대로 보내거나, 필요한 경우 DTO를 추가해야 함.
        // 여기서는 List<MessageUnreadCountResp>를 그대로 전송.
        String destination = String.format("/topic/%s.rooms.%d", type.name().toLowerCase(), roomId);
        messagingTemplate.convertAndSend(destination, updates);
        log.debug("UnreadCount updates sent to destination={}: count={}", destination, updates.size());
    }

    // 채팅방 구독자들에게 메시지 전송
    public void sendChatMessage(Long roomId, ChatRoomType type, MessageResp messageResp) {
        String destination = String.format("/topic/%s.rooms.%d", type.name().toLowerCase(), roomId);
        messagingTemplate.convertAndSend(destination, messageResp);
        log.debug("Message sent to destination={}: {}", destination, messageResp.id());
    }

    // 채팅방 리스트 업데이트 전송 (해당 채팅방 멤버들에게 개별 전송)
    public void sendRoomListUpdateBroadcast(RoomLastMessageUpdateResp updateResp) {
        // 1. 현재 채팅방을 보고 있는 사람들에게 전송 (page.tsx에서 처리)
        String roomDestination = String.format("/topic/%s.rooms.%d",
                updateResp.chatRoomType().name().toLowerCase(),
                updateResp.roomId());
        messagingTemplate.convertAndSend(roomDestination, updateResp);

        // 2. 해당 채팅방의 모든 멤버에게 개인 큐로 전송 (layout.tsx에서 처리)
        List<Long> memberIds = chatRoomMemberRepository
                .findByChatRoomIdAndChatRoomType(updateResp.roomId(), updateResp.chatRoomType())
                .stream()
                .map(chatMember -> chatMember.getMember().getId())
                .toList();

        for (Long memberId : memberIds) {
            // 1. Standard User Destination (via Broker Relay or SimpleBroker)
            String userDestination = "/queue/rooms.update";
            messagingTemplate.convertAndSendToUser(
                    memberId.toString(),
                    userDestination,
                    updateResp
            );
            
            // 2. Topic-based Fallback (reliable even if User Destinations are misconfigured)
            String topicDestination = String.format("/topic/users.%d.rooms.update", memberId);
            messagingTemplate.convertAndSend(topicDestination, updateResp);

            log.info("[RoomListUpdate] Sent to user={}, dest1=/user/{}/queue/rooms.update, dest2={}", 
                    memberId, memberId, topicDestination);
        }

        log.info("[RoomListUpdate] Sent to room={} and {} members, roomId={}, type={}, senderId={}, sequence={}",
                roomDestination, memberIds.size(), updateResp.roomId(), updateResp.chatRoomType(),
                updateResp.senderId(), updateResp.latestSequence());
    }
}