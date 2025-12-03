package triplestar.mixchat.domain.chat.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemMessageService {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void sendJoinMessage(Long roomId, String nickname, ChatRoomType type) {
        sendSystemMessage(roomId, type, "MEMBER_JOINED", Map.of("nickname", nickname));
    }

    @Transactional
    public void sendLeaveMessage(Long roomId, String nickname, ChatRoomType type) {
        sendSystemMessage(roomId, type, "MEMBER_LEFT", Map.of("nickname", nickname));
    }

    @Transactional
    public void sendKickMessage(Long roomId, String nickname, ChatRoomType type) {
        sendSystemMessage(roomId, type, "MEMBER_KICKED", Map.of("nickname", nickname));
    }

    @Transactional
    public void sendOwnerChangedMessage(Long roomId, String oldOwner, String newOwner, ChatRoomType type) {
        sendSystemMessage(roomId, type, "OWNER_CHANGED", Map.of("oldOwner", oldOwner, "newOwner", newOwner));
    }

    @Transactional
    public void sendDirectChatStartedMessage(Long roomId, ChatRoomType type) {
        sendSystemMessage(roomId, type, "DIRECT_CHAT_STARTED", Map.of());
    }

    private void sendSystemMessage(Long roomId, ChatRoomType type, String eventType, Map<String, String> params) {
        try {
            Map<String, Object> contentMap = Map.of(
                    "type", eventType,
                    "params", params
            );
            String content = objectMapper.writeValueAsString(contentMap);

            // 시스템 메시지 저장 (senderId=0L, senderName="System")
            MessageResp systemMessage = chatMessageService.saveMessage(
                    roomId, 0L, "System", content,
                    ChatMessage.MessageType.SYSTEM, type, false
            );

            // 브로드캐스트
            String destination = "/topic/" + type.name().toLowerCase() + "/rooms/" + roomId;
            messagingTemplate.convertAndSend(destination, systemMessage);

        } catch (JsonProcessingException e) {
            log.error("시스템 메시지 생성 실패: roomId={}, event={}", roomId, eventType, e);
        }
    }
}