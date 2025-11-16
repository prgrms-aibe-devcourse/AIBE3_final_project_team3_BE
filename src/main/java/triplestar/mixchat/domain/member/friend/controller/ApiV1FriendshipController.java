package triplestar.mixchat.domain.member.friend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.member.friend.dto.FriendshipSendReq;
import triplestar.mixchat.domain.member.friend.service.FriendshipRequestService;
import triplestar.mixchat.domain.member.friend.service.FriendshipService;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/members/friends")
public class ApiV1FriendshipController implements ApiFriendshipController {

    private final FriendshipRequestService friendshipRequestService;
    private final FriendshipService friendshipService;

    @Override
    @PostMapping
    public CustomResponse<Long> sendFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FriendshipSendReq req
    ) {
        Long memberId = userDetails.getId();
        Long requestId = friendshipRequestService.sendRequest(memberId, req.receiverId());

        return CustomResponse.ok("친구 요청이 성공적으로 전송되었습니다.", requestId);
    }

    @Override
    @PatchMapping("/{requestId}/accept")
    public CustomResponse<Void> acceptRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long requestId
    ) {
        Long memberId = userDetails.getId();
        friendshipRequestService.processRequest(memberId, requestId, true);

        return CustomResponse.ok("친구 요청이 수락되었습니다.");
    }

    @Override
    @PatchMapping("/{requestId}/reject")
    public CustomResponse<Void> rejectRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long requestId
    ) {
        Long memberId = userDetails.getId();
        friendshipRequestService.processRequest(memberId, requestId, false);

        return CustomResponse.ok("친구 요청이 거절되었습니다.");
    }

    @Override
    @DeleteMapping("/{friendId}")
    public CustomResponse<Void> deleteFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long friendId
    ) {
        Long memberId = userDetails.getId();
        friendshipService.deleteFriendship(memberId, friendId);

        return CustomResponse.ok("친구가 성공적으로 삭제되었습니다.");
    }
}
