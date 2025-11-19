package triplestar.mixchat.domain.chat.chat.controller;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import triplestar.mixchat.domain.chat.chat.dto.MessageReq;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.global.security.CustomUserDetails;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ApiV1ChatSocketController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/chats/sendMessage")
    public void sendMessage(@Payload MessageReq messageReq, Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication principal not found.");
        }

        Authentication authentication = (Authentication) principal;
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long senderId = userDetails.getId();
        String senderNickname = userDetails.getNickname();

        // 1. 사용자가 해당 채팅방에 메시지를 보낼 권한이 있는지 확인 (캐시 우선 조회)
        chatRoomService.verifyUserIsMemberOfRoom(senderId, messageReq.roomId());

        // 2. 권한이 확인되면 메시지 저장 및 전송 (DB 조회 없음)
        MessageResp messageResp = chatMessageService.saveMessage(
                messageReq.roomId(),
                senderId,
                senderNickname,
                messageReq.content(),
                messageReq.messageType()
        );

        messagingTemplate.convertAndSend("/topic/rooms/" + messageReq.roomId(), messageResp);
        log.debug("Message sent to room {}: {}", messageReq.roomId(), messageResp.content());
    }
}
