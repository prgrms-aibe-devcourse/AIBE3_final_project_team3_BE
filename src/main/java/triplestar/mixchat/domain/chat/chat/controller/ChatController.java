package triplestar.mixchat.domain.chat.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.chat.chat.dto.*;
import triplestar.mixchat.domain.chat.dto.*;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;
    private final SimpMessagingTemplate messagingTemplate;

    private Member getCurrentMember(CustomUserDetails currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("인증된 사용자 정보가 없습니다.");
        }
        return memberRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @PostMapping("/rooms/direct")
    public ChatRoomResp createDirectRoom(@AuthenticationPrincipal CustomUserDetails currentUser,
                                         @Valid @RequestBody CreateDirectChatReq request) {
        Member member = getCurrentMember(currentUser);
        ChatRoom room = chatRoomService.findOrCreateDirectRoom(member, request.partnerId());
        return ChatRoomResp.from(room);
    }

    @PostMapping("/rooms/group")
    public ChatRoomResp createGroupRoom(@AuthenticationPrincipal CustomUserDetails currentUser,
                                        @Valid @RequestBody CreateGroupChatReq request) {
        Member member = getCurrentMember(currentUser);
        ChatRoom room = chatRoomService.createGroupRoom(request.roomName(), request.memberIds(), member);
        return ChatRoomResp.from(room);
    }

    @PostMapping("/rooms/public")
    public ChatRoomResp createPublicGroupRoom(@AuthenticationPrincipal CustomUserDetails currentUser,
                                              @Valid @RequestBody CreatePublicChatReq request) {
        Member member = getCurrentMember(currentUser);
        ChatRoom room = chatRoomService.createPublicGroupRoom(request.roomName(), member);
        return ChatRoomResp.from(room);
    }

    @GetMapping("/rooms")
    public List<ChatRoomResp> getRooms(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Member member = getCurrentMember(currentUser);
        return chatRoomService.getRoomsForUser(member).stream()
                .map(ChatRoomResp::from)
                .collect(Collectors.toList());
    }

    @PostMapping("/rooms/{roomId}/message")
    public ChatMessage sendMessage(@PathVariable Long roomId,
                                   @RequestParam Long memberId,
                                   @RequestBody String content) {
        ChatRoom room = chatRoomService.getRoom(roomId);
        Member member = memberRepository.getReferenceById(memberId);
        return chatMessageService.saveMessage(room, member, content, ChatMessage.MessageType.TEXT);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<MessageResponse> getMessages(@PathVariable Long roomId) {
        List<ChatMessage> messages = chatMessageService.getMessages(roomId);

        List<Long> senderIds = messages.stream()
                .map(ChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());

        java.util.Map<Long, Member> membersById = memberRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        return messages.stream()
                .map(message -> {
                    Member sender = membersById.get(message.getSenderId());
                    String senderName = (sender != null) ? sender.getNickname() : "Unknown";
                    return MessageResponse.from(message, senderName);
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/rooms/{roomId}/files")
    public ResponseEntity<MessageResponse> uploadFile(@PathVariable Long roomId,
                                                      @AuthenticationPrincipal CustomUserDetails currentUser,
                                                      @RequestParam("file") MultipartFile file,
                                                      @RequestParam("messageType") ChatMessage.MessageType messageType) {
        Member member = getCurrentMember(currentUser);
        ChatRoom room = chatRoomService.getRoom(roomId);

        String fileUrl = s3Uploader.uploadFile(file, "chat-files"); // "chat-files"는 S3 버킷 내 디렉토리 이름
        ChatMessage savedMessage = chatMessageService.saveFileMessage(room, member, fileUrl, messageType);

        // WebSocket으로 메시지 전송
        String senderName = member.getNickname() != null ? member.getNickname() : member.getEmail(); // 닉네임이 없으면 이메일 사용
        MessageResponse messageResponse = MessageResponse.from(savedMessage, senderName);
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, messageResponse);

        return ResponseEntity.ok(messageResponse);
    }

    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Member member = getCurrentMember(currentUser);
        chatRoomService.leaveRoom(roomId, member);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Member member = getCurrentMember(currentUser);
        chatRoomService.blockUser(roomId, member);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/report")
    public ResponseEntity<Void> reportRoom(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Member member = getCurrentMember(currentUser);
        chatRoomService.reportRoom(roomId, member);
        return ResponseEntity.ok().build();
    }
}