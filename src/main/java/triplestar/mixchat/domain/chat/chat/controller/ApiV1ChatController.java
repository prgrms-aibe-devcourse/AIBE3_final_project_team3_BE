package triplestar.mixchat.domain.chat.chat.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackResp;
import triplestar.mixchat.domain.ai.systemprompt.service.AiFeedbackService;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.AIChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.ChatRoomPageDataResp;
import triplestar.mixchat.domain.chat.chat.dto.CreateAIChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreateGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.DirectChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.GroupChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.InviteGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.JoinGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.MessagePageResp;
import triplestar.mixchat.domain.chat.chat.dto.MessageReq;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.dto.TransferOwnerReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.service.AIChatRoomService;
import triplestar.mixchat.domain.chat.chat.service.ChatMemberService;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.DirectChatRoomService;
import triplestar.mixchat.domain.chat.chat.service.GroupChatRoomService;
import triplestar.mixchat.domain.chat.chat.service.LoadTestCleanupService;
import triplestar.mixchat.global.response.CustomResponse;
import java.util.Map;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ApiV1ChatController implements ApiChatController {

    private final DirectChatRoomService directChatRoomService;
    private final GroupChatRoomService groupChatRoomService;
    private final AIChatRoomService aiChatRoomService;
    private final ChatMemberService chatMemberService;
    private final ChatMessageService chatMessageService;
    private final LoadTestCleanupService loadTestCleanupService;
    private final S3Uploader s3Uploader;
    private final SimpMessagingTemplate messagingTemplate;
    private final AiFeedbackService aiFeedbackService;

    @Override
    @PostMapping("/feedback")
    public CustomResponse<AiFeedbackResp> analyzeFeedback(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody AiFeedbackReq req
    ) {
        AiFeedbackResp response = aiFeedbackService.analyze(req);
        return CustomResponse.ok("AI 피드백 분석 성공", response);
    }

    @Override
    @PostMapping("/rooms/direct")
    public CustomResponse<DirectChatRoomResp> createDirectRoom(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateDirectChatReq request
    ) {
        DirectChatRoomResp roomResp =
                directChatRoomService.findOrCreateDirectChatRoom(currentUser.getId(), request.partnerId(),
                        currentUser.getNickname());
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

    @Override
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
        return CustomResponse.ok("사용자가 속한 그룹 채팅방 목록 조회에 성공하였습니다.", rooms);
    }

    // todo: 비밀번호 걸린 방도 public 조회는 혼동 여지 존재. 위를 me로 바꾸고 아래를 group으로 고려
    @GetMapping("/rooms/group/public")
    public CustomResponse<List<GroupChatRoomResp>> getPublicGroupChatRooms(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<GroupChatRoomResp> rooms = groupChatRoomService.getGroupPublicRooms(currentUser.getId());
        return CustomResponse.ok("공개 그룹 채팅방 목록 조회에 성공하였습니다.", rooms);
    }

    @PostMapping("/rooms/group/{roomId}/join")
    public CustomResponse<GroupChatRoomResp> joinGroupRoom(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody(required = false) JoinGroupChatReq request
    ) {
        String password = request != null ? request.password() : null;
        GroupChatRoomResp roomResp = groupChatRoomService.joinGroupRoom(roomId, currentUser.getId(), password);
        return CustomResponse.ok("그룹 채팅방 참가에 성공하였습니다.", roomResp);
    }

    @PostMapping("/rooms/group/{roomId}/invite")
    public CustomResponse<Void> inviteMember(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody InviteGroupChatReq request
    ) {
        groupChatRoomService.inviteMember(roomId, currentUser.getId(), request.targetMemberId());
        return CustomResponse.ok("멤버를 초대했습니다.", null);
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
    @GetMapping("/rooms/{roomId}/messages")
    public CustomResponse<ChatRoomPageDataResp> getMessages(
            @PathVariable("roomId") Long roomId,
            @RequestParam ChatRoomType chatRoomType,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        // 메시지 조회 전에 읽음 처리 (채팅방 입장 시 자동 읽음)
        chatMemberService.markAsReadOnEnter(currentUser.getId(), roomId, chatRoomType);

        MessagePageResp messagePageResp = chatMessageService.getMessagesWithSenderInfo(roomId, chatRoomType,
                currentUser.getId(), cursor, size);
        ChatRoomPageDataResp responseData = ChatRoomPageDataResp.of(chatRoomType, messagePageResp);
        return CustomResponse.ok("메시지 목록과 대화 타입 조회에 성공하였습니다.", responseData);
    }

    @Override
    @PostMapping(value = "/rooms/{roomId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CustomResponse<MessageResp> uploadFile(
            @PathVariable("roomId") Long roomId,
            @RequestParam ChatRoomType chatRoomType,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam("messageType") ChatMessage.MessageType messageType
    ) {
        String fileUrl = s3Uploader.uploadFile(file, "chat-files");
        MessageResp messageResp =
                chatMessageService.saveFileMessage(roomId, currentUser.getId(), currentUser.getNickname(), fileUrl,
                        messageType, chatRoomType);

        String destination = "/topic/" + chatRoomType.name().toLowerCase() + "/rooms/" + roomId;
        messagingTemplate.convertAndSend(destination, messageResp);

        return CustomResponse.ok("파일 업로드 및 메시지 전송에 성공하였습니다.", messageResp);
    }

    @Override
    @DeleteMapping("/rooms/{roomId}")
    public void leaveRoom(
            @PathVariable("roomId") Long roomId,
            @RequestParam ChatRoomType chatRoomType,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        if (chatRoomType == ChatRoomType.DIRECT) {
            directChatRoomService.leaveRoom(roomId, currentUser.getId());
        } else if (chatRoomType == ChatRoomType.GROUP) {
            groupChatRoomService.leaveRoom(roomId, currentUser.getId());
        } else if (chatRoomType == ChatRoomType.AI) {
            aiChatRoomService.leaveAIChatRoom(roomId, currentUser.getId());
        } else {
            throw new IllegalArgumentException("지원하지 않는 대화 타입입니다: " + chatRoomType);
        }
    }

    @DeleteMapping("/rooms/{roomId}/members/{memberId}")
    public CustomResponse<Void> kickMember(
            @PathVariable Long roomId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        groupChatRoomService.kickMember(roomId, currentUser.getId(), memberId);
        return CustomResponse.ok("멤버를 강퇴했습니다.", null);
    }

    @PatchMapping("/rooms/{roomId}/owner")
    public CustomResponse<Void> transferOwnership(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody TransferOwnerReq request
    ) {
        groupChatRoomService.transferOwnership(roomId, currentUser.getId(), request.newOwnerId());
        return CustomResponse.ok("방장을 위임했습니다.", null);
    }

    @Override
    @PostMapping("/rooms/{roomId}/block")
    public void blockUser(
            @PathVariable("roomId") Long roomId,
            @RequestParam ChatRoomType chatRoomType,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        // chatMemberService.blockUser(currentUser.getId(), null, roomId, chatRoomType);
    }

    @Override
    @PostMapping("/rooms/{roomId}/reportUser")
    public void reportUser(
            @PathVariable Long roomId,
            @RequestParam ChatRoomType chatRoomType,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        // chatMemberService.reportUser(currentUser.getId(), null, roomId, chatRoomType, null);
    }

    @Override
    @PostMapping("/rooms/messages")
    @Profile({"dev", "local", "test"})  // 개발/로컬/테스트 환경에서만 활성화
    public CustomResponse<MessageResp> sendMessageForLoadTest(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody MessageReq request
    ) {
        // 멤버 검증은 saveMessage 내부에서 수행됨
        MessageResp resp = chatMessageService.saveMessage(
                request.roomId(),
                currentUser.getId(),
                currentUser.getNickname(),
                request.content(),
                request.messageType(),
                request.chatRoomType(),
                request.isTranslateEnabled()
        );
        return CustomResponse.ok("메시지 전송 완료", resp);
    }

    @Override
    @PostMapping("/loadtest/cleanup")
    @Profile({"dev", "local", "test"})  // 개발/로컬/테스트 환경에서만 활성화
    public CustomResponse<?> cleanupLoadTestData(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        long deletedCount = loadTestCleanupService.cleanupLoadTestData();
        return CustomResponse.ok("부하테스트 데이터 정리 완료", Map.of("deletedCount", deletedCount));
    }
}
