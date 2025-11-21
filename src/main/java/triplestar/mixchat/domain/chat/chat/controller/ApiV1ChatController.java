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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.chat.chat.dto.AIChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.CreateAIChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreateGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.ChatRoomDataResp;
import triplestar.mixchat.domain.chat.chat.dto.DirectChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.GroupChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.dto.TextMessageReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.service.AIChatRoomService;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatInteractionService;
import triplestar.mixchat.domain.chat.chat.service.DirectChatRoomService;
import triplestar.mixchat.domain.chat.chat.service.GroupChatRoomService;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ApiV1ChatController implements ApiChatController {

    private final DirectChatRoomService directChatRoomService;
    private final GroupChatRoomService groupChatRoomService;
    private final AIChatRoomService aiChatRoomService;
    private final ChatInteractionService chatInteractionService;
    private final ChatMessageService chatMessageService;
    private final S3Uploader s3Uploader;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @PostMapping("/rooms/direct")
    public CustomResponse<DirectChatRoomResp> createDirectRoom(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateDirectChatReq request
    ) {
        DirectChatRoomResp roomResp =
                directChatRoomService.findOrCreateDirectChatRoom(currentUser.getId(), request.partnerId(), currentUser.getNickname());
        return CustomResponse.ok("1:1 채팅방 생성/조회에 성공하였습니다.", roomResp);
    }

    @Override
    @PostMapping("/rooms/group")
    public CustomResponse<GroupChatRoomResp> createGroupRoom(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateGroupChatReq request
    ) {
        GroupChatRoomResp roomResp =
                groupChatRoomService.createGroupRoom(request, currentUser.getId());
        return CustomResponse.ok("그룹 채팅방 생성에 성공하였습니다.", roomResp);
    }

    @PostMapping("/rooms/ai")
    public CustomResponse<AIChatRoomResp> createAiRoom(
        @AuthenticationPrincipal CustomUserDetails currentUser,
        @Valid @RequestBody CreateAIChatReq request
    ) {
        AIChatRoomResp roomResp =
            aiChatRoomService.createAIChatRoom(currentUser.getId(), request);
        return CustomResponse.ok("AI 채팅방 생성에 성공하였습니다.", roomResp);
    }

    @Override
    @GetMapping("/rooms/direct")
    public CustomResponse<List<DirectChatRoomResp>> getDirectChatRooms(
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<DirectChatRoomResp> rooms = directChatRoomService.getRoomsForUser(currentUser.getId());
        return CustomResponse.ok("1:1 채팅방 목록 조회에 성공하였습니다.", rooms);
    }

    @Override
    @GetMapping("/rooms/group")
    public CustomResponse<List<GroupChatRoomResp>> getGroupChatRooms(
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<GroupChatRoomResp> rooms = groupChatRoomService.getRoomsForUser(currentUser.getId());
        return CustomResponse.ok("그룹 채팅방 목록 조회에 성공하였습니다.", rooms);
    }

    @Override
    @GetMapping("/rooms/ai")
    public CustomResponse<List<AIChatRoomResp>> getAiChatRooms(
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<AIChatRoomResp> rooms = aiChatRoomService.getRoomsForUser(currentUser.getId());
        return CustomResponse.ok("AI 채팅방 목록 조회에 성공하였습니다.", rooms);
    }


    @Override
    @PostMapping("/rooms/{roomId}/message")
    public CustomResponse<MessageResp> sendMessage(
            @PathVariable("roomId") Long roomId,
            @RequestParam ChatMessage.chatRoomType chatRoomType,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody TextMessageReq request
    ) {
        MessageResp messageResp =
                chatMessageService.saveMessage(roomId, currentUser.getId(), currentUser.getNickname(), request.content(), ChatMessage.MessageType.TEXT, chatRoomType);
        return CustomResponse.ok("메시지 전송에 성공하였습니다.", messageResp);
    }

    @Override
    @GetMapping("/rooms/{roomId}/messages")
    public CustomResponse<ChatRoomDataResp> getMessages(
            @PathVariable("roomId") Long roomId,
            @RequestParam ChatMessage.chatRoomType chatRoomType
    ) {
        List<MessageResp> messageResps = chatMessageService.getMessagesWithSenderInfo(roomId, chatRoomType);
        ChatRoomDataResp responseData = new ChatRoomDataResp(chatRoomType, messageResps);
        return CustomResponse.ok("메시지 목록과 대화 타입 조회에 성공하였습니다.", responseData);
    }

    @Override
    @PostMapping(value = "/rooms/{roomId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CustomResponse<MessageResp> uploadFile(
            @PathVariable("roomId") Long roomId,
            @RequestParam ChatMessage.chatRoomType chatRoomType,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam("messageType") ChatMessage.MessageType messageType
    ) {
        String fileUrl = s3Uploader.uploadFile(file, "chat-files");
        MessageResp messageResp =
                chatMessageService.saveFileMessage(roomId, currentUser.getId(), currentUser.getNickname(), fileUrl, messageType, chatRoomType);

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
            @RequestParam ChatMessage.chatRoomType chatRoomType, // chatRoomType 추가
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        if (chatRoomType == ChatMessage.chatRoomType.DIRECT) {
            directChatRoomService.leaveRoom(roomId, currentUser.getId());
        } else if (chatRoomType == ChatMessage.chatRoomType.GROUP) {
            groupChatRoomService.leaveRoom(roomId, currentUser.getId());
        } else if (chatRoomType == ChatMessage.chatRoomType.AI) {
            aiChatRoomService.leaveAIChatRoom(roomId, currentUser.getId());
        } else {
            throw new IllegalArgumentException("지원하지 않는 대화 타입입니다: " + chatRoomType);
        }
    }

    @Override
    @PostMapping("/rooms/{roomId}/block")
    public void blockUser(
            @PathVariable("roomId") Long roomId,
            @RequestParam ChatMessage.chatRoomType chatRoomType,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        chatInteractionService.blockUser(currentUser.getId(), null, roomId, chatRoomType);
    }

    @Override
    @PostMapping("/rooms/{roomId}/reportUser")
    public void reportUser(
            @PathVariable Long roomId,
            @RequestParam ChatMessage.chatRoomType chatRoomType,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        chatInteractionService.reportUser(currentUser.getId(), null, roomId, chatRoomType, null);
    }
}
