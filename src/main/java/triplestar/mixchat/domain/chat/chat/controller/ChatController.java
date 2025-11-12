package triplestar.mixchat.domain.chat.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.chat.chat.dto.*;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.global.response.ApiResponse;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final S3Uploader s3Uploader;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/rooms/direct")
    public ApiResponse<ChatRoomResp> createDirectRoom(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                      @Valid @RequestBody CreateDirectChatReq request) {
        ChatRoomResp roomResp = chatRoomService.findOrCreateDirectRoom(currentUser.getId(), request.partnerId());
        return ApiResponse.ok("1:1 채팅방 생성/조회에 성공하였습니다.", roomResp);
    }

    @PostMapping("/rooms/group")
    public ApiResponse<ChatRoomResp> createGroupRoom(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                     @Valid @RequestBody CreateGroupChatReq request) {
        ChatRoomResp roomResp = chatRoomService.createGroupRoom(request.roomName(), request.memberIds(), currentUser.getId());
        return ApiResponse.ok("그룹 채팅방 생성에 성공하였습니다.", roomResp);
    }

    @PostMapping("/rooms/public")
    public ApiResponse<ChatRoomResp> createPublicGroupRoom(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                           @Valid @RequestBody CreatePublicChatReq request) {
        ChatRoomResp roomResp = chatRoomService.createPublicGroupRoom(request.roomName(), currentUser.getId());
        return ApiResponse.ok("공개 그룹 채팅방 생성에 성공하였습니다.", roomResp);
    }

    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomResp>> getRooms(@AuthenticationPrincipal CustomUserDetails currentUser) {
        List<ChatRoomResp> rooms = chatRoomService.getRoomsForUser(currentUser.getId());
        return ApiResponse.ok("채팅방 목록 조회에 성공하였습니다.", rooms);
    }

    @PostMapping("/rooms/{roomId}/message")
    public ApiResponse<MessageResp> sendMessage(@PathVariable Long roomId,
                                                    @AuthenticationPrincipal CustomUserDetails currentUser,
                                                    @RequestBody String content) {
        MessageResp messageResp = chatMessageService.saveMessage(roomId, currentUser.getId(), content, ChatMessage.MessageType.TEXT);
        return ApiResponse.ok("메시지 전송에 성공하였습니다.", messageResp);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ApiResponse<List<MessageResp>> getMessages(@PathVariable Long roomId) {
        List<MessageResp> messageResps = chatMessageService.getMessagesWithSenderInfo(roomId);
        return ApiResponse.ok("메시지 목록 조회에 성공하였습니다.", messageResps);
    }

    @PostMapping("/rooms/{roomId}/files")
    public ApiResponse<MessageResp> uploadFile(@PathVariable Long roomId,
                                                   @AuthenticationPrincipal CustomUserDetails currentUser,
                                                   @RequestParam("file") MultipartFile file,
                                                   @RequestParam("messageType") ChatMessage.MessageType messageType) {
        String fileUrl = s3Uploader.uploadFile(file, "chat-files");
        MessageResp messageResp = chatMessageService.saveFileMessage(roomId, currentUser.getId(), fileUrl, messageType);

        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, messageResp);

        return ApiResponse.ok("파일 업로드 및 메시지 전송에 성공하였습니다.", messageResp);
    }

    @DeleteMapping("/rooms/{roomId}/leave")
    public void leaveRoom(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        chatRoomService.leaveRoom(roomId, currentUser.getId());
    }

    @PostMapping("/rooms/{roomId}/block")
    public void blockUser(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        chatRoomService.blockUser(roomId, currentUser.getId());
    }

    @PostMapping("/rooms/{roomId}/reportUser")
    public void reportUser(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        chatRoomService.reportUser(roomId, currentUser.getId());
    }
}
