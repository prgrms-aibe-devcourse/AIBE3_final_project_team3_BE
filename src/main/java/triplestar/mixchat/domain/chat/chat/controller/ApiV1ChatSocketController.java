package triplestar.mixchat.domain.chat.chat.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import triplestar.mixchat.domain.chat.chat.dto.MessageReq;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.global.security.CustomUserDetails;

//채팅 웹소켓을 위한 컨트롤러
@Controller
@RequiredArgsConstructor
public class ApiV1ChatSocketController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/chats/sendMessage")
    public void sendMessage(@Payload MessageReq messageReq, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long senderId = userDetails.getId();

        MessageResp messageResp = chatMessageService.saveMessage(messageReq.roomId(), senderId, messageReq.content(), messageReq.messageType());

        messagingTemplate.convertAndSend("/topic/rooms/" + messageReq.roomId(), messageResp);
    }
}
