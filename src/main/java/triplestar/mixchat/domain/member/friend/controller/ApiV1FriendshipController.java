package triplestar.mixchat.domain.member.friend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.member.friend.service.FriendshipRequestService;
import triplestar.mixchat.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "ApiV1FriendshipController", description = "API 친구관계 컨트롤러")
@RequestMapping("api/v1/member/friends")
public class ApiV1FriendshipController {

    private final FriendshipRequestService friendshipRequestService;

    @PostMapping
    public ApiResponse<Long> sendFriendRequest(
            @AuthenticationPrincipal Long memberId,
            @RequestBody Long receiverId
    ) {
        Long requestId = friendshipRequestService.sendRequest(memberId, receiverId);
        return ApiResponse.ok("친구 요청이 성공적으로 전송되었습니다.", requestId);
    }

    @PatchMapping("/{requestId}/accept")
    public ApiResponse<Void> acceptRequest(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long requestId
    ) {
        friendshipRequestService.processRequest(memberId, requestId, true);
        return ApiResponse.ok("친구 요청이 수락되었습니다.");
    }

    @PatchMapping("/{requestId}/reject")
    public ApiResponse<Void> rejectRequest(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long requestId
    ) {
        friendshipRequestService.processRequest(memberId, requestId, false);
        return ApiResponse.ok("친구 요청이 거절되었습니다.");
    }
}
