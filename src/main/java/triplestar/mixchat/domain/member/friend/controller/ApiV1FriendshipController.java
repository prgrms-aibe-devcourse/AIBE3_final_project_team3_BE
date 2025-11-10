package triplestar.mixchat.domain.member.friend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.member.friend.service.FriendshipRequestService;
import triplestar.mixchat.domain.member.friend.service.FriendshipService;
import triplestar.mixchat.global.response.ApiResponse;
import triplestar.mixchat.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@Tag(name = "ApiV1FriendshipController", description = "API 친구관계 컨트롤러")
@RequestMapping("api/v1/member/friends")
public class ApiV1FriendshipController {

    private final FriendshipRequestService friendshipRequestService;
    private final FriendshipService friendshipService;

    @PostMapping
    public ApiResponse<Long> sendFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Long receiverId
    ) {
        Long memberId = userDetails.getId();
        Long requestId = friendshipRequestService.sendRequest(memberId, receiverId);

        return ApiResponse.ok("친구 요청이 성공적으로 전송되었습니다.", requestId);
    }

    @PatchMapping("/{requestId}/accept")
    public ApiResponse<Void> acceptRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long requestId
    ) {
        Long memberId = userDetails.getId();
        friendshipRequestService.processRequest(memberId, requestId, true);

        return ApiResponse.ok("친구 요청이 수락되었습니다.");
    }

    @PatchMapping("/{requestId}/reject")
    public ApiResponse<Void> rejectRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long requestId
    ) {
        Long memberId = userDetails.getId();
        friendshipRequestService.processRequest(memberId, requestId, false);

        return ApiResponse.ok("친구 요청이 거절되었습니다.");
    }

    @DeleteMapping("/{friendId}")
    public ApiResponse<Void> deleteFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long friendId
    ) {
        Long memberId = userDetails.getId();
        friendshipService.deleteFriendship(memberId, friendId);

        return ApiResponse.ok("친구가 성공적으로 삭제되었습니다.");
    }
}
