package triplestar.mixchat.domain.chat.chat.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import triplestar.mixchat.domain.chat.chat.dto.MessageRequest;
import triplestar.mixchat.domain.chat.chat.dto.MessageResponse;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.security.CustomUserDetails;

import java.security.Principal;

//채팅 웹소켓을 위한 컨트롤러
@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final MemberRepository memberRepository;

    private Member getMemberFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("사용자 인증 정보 없음");
        }
        CustomUserDetails customUserDetails = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        return memberRepository.findById(customUserDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + customUserDetails.getId()));
    }

    @MessageMapping("/chats/sendMessage")
    public void sendMessage(@Payload MessageRequest messageRequest, Principal principal) {
        Member member = getMemberFromPrincipal(principal);
        ChatRoom room = chatRoomService.getRoom(messageRequest.getRoomId());

        ChatMessage savedMessage = chatMessageService.saveMessage(room, member, messageRequest.getContent(), messageRequest.getMessageType());

        MessageResponse messageResponse = MessageResponse.from(savedMessage, member.getNickname());
        messagingTemplate.convertAndSend("/topic/rooms/" + messageRequest.getRoomId(), messageResponse);
    }
}