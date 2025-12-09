package triplestar.mixchat.domain.chat.chat.controller;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import triplestar.mixchat.domain.chat.chat.dto.MessageReq;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.service.ChatMemberService;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.global.security.CustomUserDetails;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ApiV1ChatSocketController {

    private final ChatMemberService chatMemberService;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chats/sendMessage")
    public void sendMessage(Principal principal, @Payload MessageReq messageReq) {
        if (!(principal instanceof Authentication authentication)) {
            log.warn("인증 정보가 없이 WebSocket 메시지 전송 요청이 들어왔습니다.");
            throw new BadCredentialsException("인증 정보가 없습니다.");
        }
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

        Long senderId = currentUser.getId();
        String senderNickname = currentUser.getNickname();

        // 사용자가 해당 채팅방에 메시지를 보낼 권한이 있는지 확인
        chatMemberService.verifyUserIsMemberOfRoom(senderId, messageReq.roomId(), messageReq.chatRoomType());

        // 2. 권한이 확인되면 메시지 저장 및 전송 (서비스 내부에서 알림 처리)
        MessageResp messageResp = chatMessageService.saveMessage(
                messageReq.roomId(),
                senderId,
                senderNickname,
                messageReq.content(),
                messageReq.messageType(),
                messageReq.chatRoomType(),
                messageReq.isTranslateEnabled()
        );

        log.debug("채팅방 {}에 메시지 전송 완료: {}", messageReq.roomId(), messageResp.content());
    }
}
