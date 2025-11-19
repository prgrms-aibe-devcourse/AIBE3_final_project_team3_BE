package triplestar.mixchat.domain.chat.chat.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.chat.chat.dto.ChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreateGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreatePublicChatReq;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.dto.TextMessageReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ApiV1ChatController implements ApiChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final S3Uploader s3Uploader;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @PostMapping("/rooms/direct")
    public CustomResponse<ChatRoomResp> createDirectRoom(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateDirectChatReq request
    ) {
        ChatRoomResp roomResp =
                chatRoomService.findOrCreateDirectRoom(currentUser.getId(), request.partnerId());
        return CustomResponse.ok("1:1 채팅방 생성/조회에 성공하였습니다.", roomResp);
    }

    @Override
    @PostMapping("/rooms/group")
    public CustomResponse<ChatRoomResp> createGroupRoom(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateGroupChatReq request
    ) {
        ChatRoomResp roomResp =
                chatRoomService.createGroupRoom(request.roomName(), request.memberIds(), currentUser.getId());
        return CustomResponse.ok("그룹 채팅방 생성에 성공하였습니다.", roomResp);
    }

    @Override
    @PostMapping("/rooms/public")
    public CustomResponse<ChatRoomResp> createPublicGroupRoom(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreatePublicChatReq request
    ) {
        ChatRoomResp roomResp =
                chatRoomService.createPublicGroupRoom(request.roomName(), currentUser.getId());
        return CustomResponse.ok("공개 그룹 채팅방 생성에 성공하였습니다.", roomResp);
    }

    @Override
    @GetMapping("/rooms")
    public CustomResponse<List<ChatRoomResp>> getRooms(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<ChatRoomResp> rooms = chatRoomService.getRoomsForUser(currentUser.getId());
        return CustomResponse.ok("채팅방 목록 조회에 성공하였습니다.", rooms);
    }

    @Override
    @PostMapping("/rooms/{roomId}/message")
    public CustomResponse<MessageResp> sendMessage(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody TextMessageReq request
    ) {
        MessageResp messageResp =
                chatMessageService.saveMessage(roomId, currentUser.getId(), currentUser.getNickname(), request.content(), ChatMessage.MessageType.TEXT);
        return CustomResponse.ok("메시지 전송에 성공하였습니다.", messageResp);
    }

    @Override
    @GetMapping("/rooms/{roomId}/messages")
    public CustomResponse<List<MessageResp>> getMessages(
            @PathVariable("roomId") Long roomId
    ) {
        List<MessageResp> messageResps = chatMessageService.getMessagesWithSenderInfo(roomId);
        return CustomResponse.ok("메시지 목록 조회에 성공하였습니다.", messageResps);
    }

    @Override
    @PostMapping(value = "/rooms/{roomId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CustomResponse<MessageResp> uploadFile(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam("messageType") ChatMessage.MessageType messageType
    ) {
        String fileUrl = s3Uploader.uploadFile(file, "chat-files");
        MessageResp messageResp =
                chatMessageService.saveFileMessage(roomId, currentUser.getId(), currentUser.getNickname(), fileUrl, messageType);

        messagingTemplate.convertAndSend(
                "/topic/chat/room/" + roomId,
                messageResp
        );

        return CustomResponse.ok("파일 업로드 및 메시지 전송에 성공하였습니다.", messageResp);
    }

    @Override
    @DeleteMapping("/rooms/{roomId}/leave")
    public void leaveRoom(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        chatRoomService.leaveRoom(roomId, currentUser.getId());
    }

    @Override
    @PostMapping("/rooms/{roomId}/block")
    public void blockUser(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        chatRoomService.blockUser(roomId, currentUser.getId());
    }

    @Override
    @PostMapping("/rooms/{roomId}/reportUser")
    public void reportUser(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        chatRoomService.reportUser(roomId, currentUser.getId());
    }
}
