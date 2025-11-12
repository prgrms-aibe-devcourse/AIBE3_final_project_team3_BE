package triplestar.mixchat.domain.chat.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.chat.chat.dto.*;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

import java.util.List;

@Tag(name = "Chat API", description = "채팅 관련 API")
@CommonBadResponse
@SuccessResponse
public interface ChatApi {

    @Operation(summary = "1:1 채팅방 생성/조회", description = "특정 사용자와의 1:1 채팅방이 없으면 새로 생성하고, 있으면 기존 채팅방 정보를 반환합니다.")
    @SignInInRequireResponse
    triplestar.mixchat.global.response.ApiResponse<ChatRoomResp> createDirectRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateDirectChatReq request
    );

    @Operation(summary = "그룹 채팅방 생성", description = "지정한 사용자들과 함께 새로운 그룹 채팅방을 생성합니다.")
    @SignInInRequireResponse
    triplestar.mixchat.global.response.ApiResponse<ChatRoomResp> createGroupRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateGroupChatReq request
    );

    @Operation(summary = "공개 그룹 채팅방 생성", description = "모든 사용자가 참여할 수 있는 공개 그룹 채팅방을 생성합니다.")
    @SignInInRequireResponse
    triplestar.mixchat.global.response.ApiResponse<ChatRoomResp> createPublicGroupRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreatePublicChatReq request
    );

    @Operation(summary = "자신의 모든 채팅방 목록 조회", description = "현재 로그인한 사용자가 참여하고 있는 모든 채팅방의 목록을 반환합니다.")
    @SignInInRequireResponse
    triplestar.mixchat.global.response.ApiResponse<List<ChatRoomResp>> getRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser
    );

    @Operation(summary = "채팅 메시지 전송", description = "지정된 채팅방에 텍스트 메시지를 전송합니다.")
    @SignInInRequireResponse
    triplestar.mixchat.global.response.ApiResponse<MessageResp> sendMessage(
            @Parameter(description = "메시지를 보낼 채팅방의 ID") @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody TextMessageReq request
    );

    @Operation(summary = "채팅방 메시지 목록 조회", description = "지정된 채팅방의 모든 메시지 내역을 시간순으로 조회합니다.")
    triplestar.mixchat.global.response.ApiResponse<List<MessageResp>> getMessages(
            @Parameter(description = "메시지를 조회할 채팅방의 ID") @PathVariable Long roomId
    );

    @Operation(summary = "파일 메시지 전송 (이미지/파일)", description = "지정된 채팅방에 이미지 또는 파일을 전송합니다.")
    @SignInInRequireResponse
    triplestar.mixchat.global.response.ApiResponse<MessageResp> uploadFile(
            @Parameter(description = "파일을 보낼 채팅방의 ID") @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Parameter(description = "업로드할 파일") @RequestParam("file") MultipartFile file,
            @Parameter(description = "파일 메시지 타입 (IMAGE 또는 FILE)") @RequestParam("messageType") ChatMessage.MessageType messageType
    );

    @Operation(summary = "채팅방 나가기", description = "지정된 채팅방에서 나갑니다. 그룹 채팅방의 마지막 멤버가 나갈 경우 채팅방이 삭제될 수 있습니다.")
    @SignInInRequireResponse
    void leaveRoom(
            @Parameter(description = "나갈 채팅방의 ID") @PathVariable Long roomId,
             @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser
    );

    @Operation(summary = "채팅방 사용자 차단 (미구현)", description = "특정 사용자를 채팅방에서 차단합니다. (현재 로직 구현 안됨)")
    @SignInInRequireResponse
    void blockUser(
            @Parameter(description = "차단할 채팅방의 ID") @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser
    );

    @Operation(summary = "채팅방/사용자 신고 (미구현)", description = "불건전한 채팅방 또는 사용자를 신고합니다. (현재 로직 구현 안됨)")
    @SignInInRequireResponse
    void reportUser(
            @Parameter(description = "신고할 채팅방의 ID") @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser
    );
}

