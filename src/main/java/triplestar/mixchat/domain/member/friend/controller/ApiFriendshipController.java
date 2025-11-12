package triplestar.mixchat.domain.member.friend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import triplestar.mixchat.global.response.ApiResponse;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SecurityRequireResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;

@Tag(name = "ApiV1FriendshipController", description = "API 친구관계 컨트롤러")
// 모든 메소드에 400, 404 공통 응답 추가
@CommonBadResponse
public interface ApiFriendshipController {

    // --- 1. 친구 요청 전송 (POST) ---
    @Operation(
            summary = "친구 요청 전송",
            description = "특정 사용자에게 친구 요청을 보냅니다.",
            responses = {
                    // 200 성공 응답은 개별적으로 정의
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    // 아래와 같이 특화된 404 응답 가능
//                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                            responseCode = "404", description = "존재하지 않는 사용자 ID (MEMBER_NOT_FOUND)",
//                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
//                    ),
            }
    )
    // 401만 있음 -> 403까지 포함해야 할 경우 SecurityRequireResponse 사용
    @SignInInRequireResponse
    ApiResponse<Long> sendFriendRequest(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "친구 요청을 받을 사용자의 ID", required = true)
            Long receiverId
    );

    // --- 2. 친구 요청 수락 (PATCH /accept) ---
    @Operation(
            summary = "친구 요청 수락",
            description = "받은 친구 요청을 수락하여 친구 관계를 맺습니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    // 401, 403 포함
    @SecurityRequireResponse
    ApiResponse<Void> acceptRequest(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "처리할 친구 요청 ID", required = true)
            @PathVariable Long requestId
    );

    // --- 3. 친구 요청 거절 (PATCH /reject) ---
    @Operation(
            summary = "친구 요청 거절",
            description = "받은 친구 요청을 거절합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    @SecurityRequireResponse
    ApiResponse<Void> rejectRequest(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "처리할 친구 요청 ID", required = true)
            @PathVariable Long requestId
    );

    // --- 4. 친구 삭제 (DELETE) ---
    @SecurityRequireResponse
    @Operation(
            summary = "친구 삭제",
            description = "기존 친구 관계를 해제합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200", description = "성공",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    ApiResponse<Void> deleteFriend(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "삭제할 친구의 ID", required = true)
            @PathVariable Long friendId
    );
}