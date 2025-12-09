package triplestar.mixchat.domain.chat.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackResp;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.AIChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.ChatRoomPageDataResp;
import triplestar.mixchat.domain.chat.chat.dto.CreateAIChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreateGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.DirectChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.GroupChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.LoadTestCleanupReq;
import triplestar.mixchat.domain.chat.chat.dto.LoadTestCleanupResp;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "Chat API", description = "채팅 관련 API")
@CommonBadResponse
@SuccessResponse
@SecurityRequirement(name = "Authorization")
public interface ApiChatController {

    @Operation(summary = "AI 피드백 분석 요청", description = "원문과 번역문(또는 사용자 의도)을 비교하여 AI에게 교정 및 피드백을 요청합니다.")
    @SignInInRequireResponse
    CustomResponse<AiFeedbackResp> analyzeFeedback(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody AiFeedbackReq req
    );

    @Operation(summary = "1:1 채팅방 생성/조회", description = "특정 사용자와의 1:1 채팅방이 없으면 새로 생성하고, 있으면 기존 채팅방 정보를 반환합니다.")
    @SignInInRequireResponse
    CustomResponse<DirectChatRoomResp> createDirectRoom( // Return type changed
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateDirectChatReq request
    );

    @Operation(summary = "그룹 채팅방 생성", description = "지정한 사용자들과 함께 새로운 그룹 채팅방을 생성합니다.")
    @SignInInRequireResponse
    CustomResponse<GroupChatRoomResp> createGroupRoom( // Return type changed
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateGroupChatReq request
    );

    @Operation(summary = "AI 채팅방 생성", description = "새로운 AI 채팅방을 생성합니다.")
    @SignInInRequireResponse
    CustomResponse<AIChatRoomResp> createAiRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateAIChatReq request
    );

//    @Operation(summary = "공개 그룹 채팅방 생성", description = "모든 사용자가 참여할 수 있는 공개 그룹 채팅방을 생성합니다.")
//    @SignInInRequireResponse
//    CustomResponse<GroupChatRoomResp> createPublicGroupRoom( // Return type changed
//            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
//            @Valid @RequestBody CreatePublicChatReq request
//    );

    @Operation(summary = "자신의 1:1 채팅방 목록 조회", description = "현재 로그인한 사용자가 참여하고 있는 모든 1:1 채팅방의 목록을 반환합니다.")
    @SignInInRequireResponse
    CustomResponse<List<DirectChatRoomResp>> getDirectChatRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser
    );

    @Operation(summary = "자신의 그룹 채팅방 목록 조회", description = "현재 로그인한 사용자가 참여하고 있는 모든 그룹 채팅방의 목록을 반환합니다.")
    @SignInInRequireResponse
    CustomResponse<List<GroupChatRoomResp>> getGroupChatRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser
    );

    @Operation(summary = "자신의 AI 채팅방 목록 조회", description = "현재 로그인한 사용자가 참여하고 있는 모든 AI 채팅방의 목록을 반환합니다.")
    @SignInInRequireResponse
    CustomResponse<List<AIChatRoomResp>> getAiChatRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser
    );

    @Operation(
        summary = "채팅방 메시지 목록 조회 (페이징)",
        description = "지정된 채팅방의 메시지 내역을 페이징하여 조회합니다. cursor와 size 파라미터를 생략하면 최근 25개 메시지를 반환합니다."
    )
    CustomResponse<ChatRoomPageDataResp> getMessages(
            @Parameter(description = "메시지를 조회할 채팅방의 ID") @PathVariable Long roomId,
            @Parameter(description = "대화방 타입 (DIRECT, GROUP, AI)") @RequestParam ChatRoomType chatRoomType,
            @Parameter(description = "이전 페이지의 마지막 메시지 sequence (생략 시 최신 메시지부터 조회)") @RequestParam(required = false) Long cursor,
            @Parameter(description = "한 페이지에 조회할 메시지 개수 (기본 25, 최대 100)") @RequestParam(required = false) Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser
    );

    @Operation(summary = "파일 메시지 전송 (이미지/파일)", description = "지정된 채팅방에 이미지 또는 파일을 전송합니다.")
    @SignInInRequireResponse
    CustomResponse<MessageResp> uploadFile(
            @Parameter(description = "파일을 보낼 채팅방의 ID") @PathVariable Long roomId,
            @Parameter(description = "대화방 타입 (DIRECT, GROUP, AI)") @RequestParam ChatRoomType chatRoomType,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Parameter(description = "업로드할 파일") @RequestParam("file") MultipartFile file,
            @Parameter(description = "파일 메시지 타입 (IMAGE 또는 FILE)") @RequestParam("messageType") ChatMessage.MessageType messageType
    );

    @Operation(summary = "채팅방 나가기", description = "지정된 채팅방에서 나갑니다. 그룹 채팅방의 마지막 멤버가 나갈 경우 채팅방이 삭제될 수 있습니다.")
    @SignInInRequireResponse
    void leaveRoom(
            @Parameter(description = "나갈 채팅방의 ID") @PathVariable Long roomId,
            @Parameter(description = "대화방 타입 (DIRECT, GROUP, AI)") @RequestParam ChatRoomType chatRoomType,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser
    );

    @Operation(summary = "채팅방 사용자 차단 (미구현)", description = "특정 사용자를 채팅방에서 차단합니다. (현재 로직 구현 안됨)")
    @SignInInRequireResponse
    void blockUser(
            @Parameter(description = "차단할 채팅방의 ID") @PathVariable Long roomId,
            @Parameter(description = "대화방 타입 (DIRECT, GROUP)") @RequestParam ChatRoomType chatRoomType,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser
    );

    @Operation(summary = "채팅방/사용자 신고 (미구현)", description = "불건전한 채팅방 또는 사용자를 신고합니다. (현재 로직 구현 안됨)")
    @SignInInRequireResponse
    void reportUser(
            @Parameter(description = "신고할 채팅방의 ID") @PathVariable Long roomId,
            @Parameter(description = "대화방 타입 (DIRECT, GROUP)") @RequestParam ChatRoomType chatRoomType,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser
    );

    @Operation(summary = "부하테스트 데이터 정리", description = "부하테스트로 생성된 채팅방, 멤버, 메시지 데이터를 일괄 삭제합니다. [LOAD_TEST] 태그가 있는 Group/AI 채팅방과 테스트 계정 간의 Direct 채팅방을 삭제합니다.")
    @SignInInRequireResponse
    CustomResponse<LoadTestCleanupResp> cleanupLoadTestData(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody LoadTestCleanupReq request
    );
}

