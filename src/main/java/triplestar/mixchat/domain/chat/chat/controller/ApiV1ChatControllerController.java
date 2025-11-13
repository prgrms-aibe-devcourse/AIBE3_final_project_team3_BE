package triplestar.mixchat.domain.chat.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.chat.chat.dto.*;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ApiV1ChatControllerController implements ApiChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final S3Uploader s3Uploader;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @PostMapping("/rooms/direct")
    public CustomResponse<ChatRoomResp> createDirectRoom(CustomUserDetails currentUser, CreateDirectChatReq request) {
        ChatRoomResp roomResp = chatRoomService.findOrCreateDirectRoom(currentUser.getId(), request.partnerId());
        return CustomResponse.ok("1:1 채팅방 생성/조회에 성공하였습니다.", roomResp);
    }

    @Override
    @PostMapping("/rooms/group")
    public CustomResponse<ChatRoomResp> createGroupRoom(CustomUserDetails currentUser, CreateGroupChatReq request) {
        ChatRoomResp roomResp = chatRoomService.createGroupRoom(request.roomName(), request.memberIds(), currentUser.getId());
        return CustomResponse.ok("그룹 채팅방 생성에 성공하였습니다.", roomResp);
    }

    @Override
    @PostMapping("/rooms/public")
    public CustomResponse<ChatRoomResp> createPublicGroupRoom(CustomUserDetails currentUser, CreatePublicChatReq request) {
        ChatRoomResp roomResp = chatRoomService.createPublicGroupRoom(request.roomName(), currentUser.getId());
        return CustomResponse.ok("공개 그룹 채팅방 생성에 성공하였습니다.", roomResp);
    }

    @Override
    @GetMapping("/rooms")
    public CustomResponse<List<ChatRoomResp>> getRooms(CustomUserDetails currentUser) {
        List<ChatRoomResp> rooms = chatRoomService.getRoomsForUser(currentUser.getId());
        return CustomResponse.ok("채팅방 목록 조회에 성공하였습니다.", rooms);
    }

    @Override
    @PostMapping("/rooms/{roomId}/message")
    public CustomResponse<MessageResp> sendMessage(Long roomId, CustomUserDetails currentUser, TextMessageReq request) {
        MessageResp messageResp = chatMessageService.saveMessage(roomId, currentUser.getId(), request.content(), ChatMessage.MessageType.TEXT);
        return CustomResponse.ok("메시지 전송에 성공하였습니다.", messageResp);
    }

    @Override
    @GetMapping("/rooms/{roomId}/messages")
    public CustomResponse<List<MessageResp>> getMessages(Long roomId) {
        List<MessageResp> messageResps = chatMessageService.getMessagesWithSenderInfo(roomId);
        return CustomResponse.ok("메시지 목록 조회에 성공하였습니다.", messageResps);
    }

    @Override
    @PostMapping(value = "/rooms/{roomId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CustomResponse<MessageResp> uploadFile(Long roomId, CustomUserDetails currentUser, MultipartFile file, ChatMessage.MessageType messageType) {
        String fileUrl = s3Uploader.uploadFile(file, "chat-files");
        MessageResp messageResp = chatMessageService.saveFileMessage(roomId, currentUser.getId(), fileUrl, messageType);

        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, messageResp);

        return CustomResponse.ok("파일 업로드 및 메시지 전송에 성공하였습니다.", messageResp);
    }

    @Override
    @DeleteMapping("/rooms/{roomId}/leave")
    public void leaveRoom(Long roomId, CustomUserDetails currentUser) {
        chatRoomService.leaveRoom(roomId, currentUser.getId());
    }

    @Override
    @PostMapping("/rooms/{roomId}/block")
    public void blockUser(Long roomId, CustomUserDetails currentUser) {
        chatRoomService.blockUser(roomId, currentUser.getId());
    }

    @Override
    @PostMapping("/rooms/{roomId}/reportUser")
    public void reportUser(Long roomId, CustomUserDetails currentUser) {
        chatRoomService.reportUser(roomId, currentUser.getId());
    }
}

