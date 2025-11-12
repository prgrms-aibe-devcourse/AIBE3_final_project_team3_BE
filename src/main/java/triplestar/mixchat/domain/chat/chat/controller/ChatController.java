package triplestar.mixchat.domain.chat.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
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
public class ChatController implements ChatApi {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final S3Uploader s3Uploader;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public ApiResponse<ChatRoomResp> createDirectRoom(CustomUserDetails currentUser, CreateDirectChatReq request) {
        ChatRoomResp roomResp = chatRoomService.findOrCreateDirectRoom(currentUser.getId(), request.partnerId());
        return ApiResponse.ok("1:1 채팅방 생성/조회에 성공하였습니다.", roomResp);
    }

    @Override
    public ApiResponse<ChatRoomResp> createGroupRoom(CustomUserDetails currentUser, CreateGroupChatReq request) {
        ChatRoomResp roomResp = chatRoomService.createGroupRoom(request.roomName(), request.memberIds(), currentUser.getId());
        return ApiResponse.ok("그룹 채팅방 생성에 성공하였습니다.", roomResp);
    }

    @Override
    public ApiResponse<ChatRoomResp> createPublicGroupRoom(CustomUserDetails currentUser, CreatePublicChatReq request) {
        ChatRoomResp roomResp = chatRoomService.createPublicGroupRoom(request.roomName(), currentUser.getId());
        return ApiResponse.ok("공개 그룹 채팅방 생성에 성공하였습니다.", roomResp);
    }

    @Override
    public ApiResponse<List<ChatRoomResp>> getRooms(CustomUserDetails currentUser) {
        List<ChatRoomResp> rooms = chatRoomService.getRoomsForUser(currentUser.getId());
        return ApiResponse.ok("채팅방 목록 조회에 성공하였습니다.", rooms);
    }

    @Override
    public ApiResponse<MessageResp> sendMessage(Long roomId, CustomUserDetails currentUser, String content) {
        MessageResp messageResp = chatMessageService.saveMessage(roomId, currentUser.getId(), content, ChatMessage.MessageType.TEXT);
        return ApiResponse.ok("메시지 전송에 성공하였습니다.", messageResp);
    }

    @Override
    public ApiResponse<List<MessageResp>> getMessages(Long roomId) {
        List<MessageResp> messageResps = chatMessageService.getMessagesWithSenderInfo(roomId);
        return ApiResponse.ok("메시지 목록 조회에 성공하였습니다.", messageResps);
    }

    @Override
    public ApiResponse<MessageResp> uploadFile(Long roomId, CustomUserDetails currentUser, MultipartFile file, ChatMessage.MessageType messageType) {
        String fileUrl = s3Uploader.uploadFile(file, "chat-files");
        MessageResp messageResp = chatMessageService.saveFileMessage(roomId, currentUser.getId(), fileUrl, messageType);

        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, messageResp);

        return ApiResponse.ok("파일 업로드 및 메시지 전송에 성공하였습니다.", messageResp);
    }

    @Override
    public void leaveRoom(Long roomId, CustomUserDetails currentUser) {
        chatRoomService.leaveRoom(roomId, currentUser.getId());
    }

    @Override
    public void blockUser(Long roomId, CustomUserDetails currentUser) {
        chatRoomService.blockUser(roomId, currentUser.getId());
    }

    @Override
    public void reportUser(Long roomId, CustomUserDetails currentUser) {
        chatRoomService.reportUser(roomId, currentUser.getId());
    }
}

