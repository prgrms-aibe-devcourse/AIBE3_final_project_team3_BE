package triplestar.mixchat.domain.chat.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.chat.chat.dto.*;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.response.ApiResponse;
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
        // todo : id로 검증
        return memberRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @PostMapping("/rooms/direct")
    public ApiResponse<ChatRoomResp> createDirectRoom(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                      @Valid @RequestBody CreateDirectChatReq request) {
        Member member = getCurrentMember(currentUser);
        ChatRoom room = chatRoomService.findOrCreateDirectRoom(member, request.partnerId());
        return ApiResponse.ok("1:1 채팅방 생성/조회에 성공하였습니다.", ChatRoomResp.from(room));
    }

    @PostMapping("/rooms/group")
    public ApiResponse<ChatRoomResp> createGroupRoom(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                     @Valid @RequestBody CreateGroupChatReq request) {
        Member member = getCurrentMember(currentUser);
        ChatRoom room = chatRoomService.createGroupRoom(request.roomName(), request.memberIds(), member);
        return ApiResponse.ok("그룹 채팅방 생성에 성공하였습니다.", ChatRoomResp.from(room));
    }

    @PostMapping("/rooms/public")
    public ApiResponse<ChatRoomResp> createPublicGroupRoom(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                           @Valid @RequestBody CreatePublicChatReq request) {
        Member member = getCurrentMember(currentUser);
        ChatRoom room = chatRoomService.createPublicGroupRoom(request.roomName(), member);
        return ApiResponse.ok("공개 그룹 채팅방 생성에 성공하였습니다.", ChatRoomResp.from(room));
    }

    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomResp>> getRooms(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Member member = getCurrentMember(currentUser);
        List<ChatRoomResp> rooms = chatRoomService.getRoomsForUser(member).stream()
                .map(ChatRoomResp::from)
                .collect(Collectors.toList());
        return ApiResponse.ok("채팅방 목록 조회에 성공하였습니다.", rooms);
    }

    @PostMapping("/rooms/{roomId}/message")
    public ApiResponse<MessageResponse> sendMessage(@PathVariable Long roomId,
                                                    @RequestParam Long memberId,
                                                    @RequestBody String content) {
        ChatRoom room = chatRoomService.getRoom(roomId);
        Member member = memberRepository.getReferenceById(memberId);
        ChatMessage savedMessage = chatMessageService.saveMessage(room, member, content, ChatMessage.MessageType.TEXT);
        String senderName = member.getNickname() != null ? member.getNickname() : member.getEmail();
        return ApiResponse.ok("메시지 전송에 성공하였습니다.", MessageResponse.from(savedMessage, senderName));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ApiResponse<List<MessageResponse>> getMessages(@PathVariable Long roomId) {
        List<ChatMessage> messages = chatMessageService.getMessages(roomId);

        List<Long> senderIds = messages.stream()
                .map(ChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());

        java.util.Map<Long, Member> membersById = memberRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        List<MessageResponse> messageResponses = messages.stream()
                .map(message -> {
                    Member sender = membersById.get(message.getSenderId());
                    String senderName = (sender != null) ? sender.getNickname() : "Unknown";
                    return MessageResponse.from(message, senderName);
                })
                .collect(Collectors.toList());
        return ApiResponse.ok("메시지 목록 조회에 성공하였습니다.", messageResponses);
    }

    @PostMapping("/rooms/{roomId}/files")
    public ApiResponse<MessageResponse> uploadFile(@PathVariable Long roomId,
                                                   @AuthenticationPrincipal CustomUserDetails currentUser,
                                                   @RequestParam("file") MultipartFile file,
                                                   @RequestParam("messageType") ChatMessage.MessageType messageType) {
        Member member = getCurrentMember(currentUser);
        ChatRoom room = chatRoomService.getRoom(roomId);

        // todo : 하드 코딩 제거
        String fileUrl = s3Uploader.uploadFile(file, "chat-files");
        ChatMessage savedMessage = chatMessageService.saveFileMessage(room, member, fileUrl, messageType);

        String senderName = member.getNickname() != null ? member.getNickname() : member.getEmail();
        MessageResponse messageResponse = MessageResponse.from(savedMessage, senderName);
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, messageResponse);

        return ApiResponse.ok("파일 업로드 및 메시지 전송에 성공하였습니다.", messageResponse);
    }

    @DeleteMapping("/rooms/{roomId}/leave")
    public ApiResponse<Void> leaveRoom(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Member member = getCurrentMember(currentUser);
        chatRoomService.leaveRoom(roomId, member);
        return ApiResponse.ok("채팅방 나가기에 성공하였습니다.", null);
    }

    @PostMapping("/rooms/{roomId}/block")
    public ApiResponse<Void> blockUser(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Member member = getCurrentMember(currentUser);
        chatRoomService.blockUser(roomId, member);
        return ApiResponse.ok("사용자 차단에 성공하였습니다.", null);
    }

    @PostMapping("/rooms/{roomId}/reportUser")
    public ApiResponse<Void> reportUser(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Member member = getCurrentMember(currentUser);
        chatRoomService.reportUser(roomId, member);
        return ApiResponse.ok("유저 신고에 성공하였습니다.", null);
    }
}
