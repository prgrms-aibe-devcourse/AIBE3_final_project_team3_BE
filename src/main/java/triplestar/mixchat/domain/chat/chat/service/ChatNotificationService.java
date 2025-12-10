package triplestar.mixchat.domain.chat.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.dto.RoomLastMessageUpdateResp;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatNotificationService {

    private final SimpMessageSendingOperations messagingTemplate;

    // 채팅방 구독자들에게 메시지 전송
    public void sendChatMessage(Long roomId, ChatRoomType type, MessageResp messageResp) {
        String destination = String.format("/topic/%s.rooms.%d", type.name().toLowerCase(), roomId);
        messagingTemplate.convertAndSend(destination, messageResp);
        log.debug("Message sent to destination={}: {}", destination, messageResp.id());
    }

    // 특정 사용자에게 채팅방 목록 갱신 정보 전송 (읽지 않은 메시지 수 등)
    public void sendRoomListUpdate(String memberId, RoomLastMessageUpdateResp updateResp) {
        messagingTemplate.convertAndSendToUser(
                memberId,
                "/queue/rooms/update",
                updateResp
        );
    }
}
