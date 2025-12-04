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
import triplestar.mixchat.domain.ai.chatassist.RagTutorService;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.MessageReq;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage.MessageType;
import triplestar.mixchat.domain.chat.chat.service.ChatMemberService;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.global.ai.BotConstant;
import triplestar.mixchat.global.security.CustomUserDetails;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ApiV1ChatSocketController {

    private final ChatMemberService chatMemberService;
    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations messagingTemplate;
    // TODO : 이후 다른 AI 대화 서비스도 추가
    private final RagTutorService ragTutorService;

    @MessageMapping("/chats/sendMessage")
    public void sendMessage(@Payload MessageReq messageReq, Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication principal not found.");
        }

        Authentication authentication = (Authentication) principal;
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long senderId = userDetails.getId();
        String senderNickname = userDetails.getNickname();

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

        String destination = "/topic/" + messageReq.chatRoomType().name().toLowerCase() + "/rooms/" + messageReq.roomId();
        messagingTemplate.convertAndSend(destination, messageResp);
        log.debug("채팅방 {}에 메시지 전송 완료: {}", messageReq.roomId(), messageResp.content());

        // 3. AI 챗룸인 경우 AI 답변 호출
        if (messageReq.chatRoomType() == ChatRoomType.AI) {
            String chat = ragTutorService.chat(senderId, messageReq.roomId(), messageReq.content());

            MessageResp aiTutorResp = chatMessageService.saveMessage(
                    messageReq.roomId(),
                    BotConstant.BOT_MEMBER_ID,
                    "AI Tutor",
                    chat,
                    MessageType.TEXT,
                    messageReq.chatRoomType(),
                    false
            );
        }
        log.debug("채팅방 {}에 메시지 전송 완료: {}", messageReq.roomId(), messageResp.content());

            messagingTemplate.convertAndSend(destination, aiTutorResp);
            log.debug("채팅방 {}에 AI TUTOR 답변 전송 완료: {}", messageReq.roomId(), aiTutorResp);
        }
    }
}
