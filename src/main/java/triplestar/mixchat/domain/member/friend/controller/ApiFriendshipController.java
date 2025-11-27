package triplestar.mixchat.domain.member.friend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import triplestar.mixchat.domain.member.friend.dto.FriendDetailResp;
import triplestar.mixchat.domain.member.friend.dto.FriendSummaryResp;
import triplestar.mixchat.domain.member.friend.dto.FriendshipRequestResp;
import triplestar.mixchat.domain.member.friend.dto.FriendshipSendReq;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SecurityRequireResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "ApiV1FriendshipController", description = "API 친구관계 컨트롤러")
// 모든 메소드에 400, 404 공통 응답 추가
@CommonBadResponse
// 모든 메소드에 200 공통 응답 추가
@SuccessResponse
@SecurityRequirement(name = "Authorization")
public interface ApiFriendshipController {

    // --- 1. 친구 목록 조회 (GET /) ---
    @Operation(
            summary = "친구 목록 조회",
            description = "현재 사용자의 친구 목록을 페이지네이션으로 조회합니다."
    )
    CustomResponse<Page<FriendSummaryResp>> getFriends(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "페이지네이션 정보 (예: page=0&size=20&sort=nickname,asc)")
            Pageable pageable
    );

    // --- 2. 특정 친구 상세 정보 조회 (GET /{friendId}) ---
    @Operation(
            summary = "특정 친구 상세 정보 조회",
            description = "친구 ID를 통해 해당 친구의 상세 정보를 조회합니다."
    )
    CustomResponse<FriendDetailResp> getFriend(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "상세 정보를 조회할 친구 ID", required = true)
            @PathVariable Long friendId
    );

    // --- 3. 친구 요청 전송 (POST) ---
    @Operation(
            summary = "친구 요청 전송",
            description = "특정 사용자에게 친구 요청을 보냅니다."
    )
    // 401만 있음 -> 403까지 포함해야 할 경우 SecurityRequireResponse 사용
    @SignInInRequireResponse
    CustomResponse<FriendshipRequestResp> sendFriendRequest(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @RequestBody(description = "로그인 정보", required = true)
            FriendshipSendReq friendshipSendReq
    );

    // --- 4. 친구 요청 수락 (PATCH /requests/{requestId}/accept) ---
    @Operation(
            summary = "친구 요청 수락",
            description = "받은 친구 요청을 수락하여 친구 관계를 맺습니다."
    )
    // 401, 403 포함
    @SecurityRequireResponse
    CustomResponse<Void> acceptRequest(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "처리할 친구 요청 ID", required = true)
            @PathVariable Long requestId
    );

    // --- 5. 친구 요청 거절 (PATCH /requests/{requestId}/reject) ---
    @Operation(
            summary = "친구 요청 거절",
            description = "받은 친구 요청을 거절합니다."
    )
    @SecurityRequireResponse
    CustomResponse<Void> rejectRequest(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "처리할 친구 요청 ID", required = true)
            @PathVariable Long requestId
    );

    // --- 6. 친구 삭제 (DELETE /{friendId}) ---
    @SecurityRequireResponse
    @Operation(
            summary = "친구 삭제",
            description = "기존 친구 관계를 해제합니다."
    )
    CustomResponse<Void> deleteFriend(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "삭제할 친구의 ID", required = true)
            @PathVariable Long friendId
    );
}