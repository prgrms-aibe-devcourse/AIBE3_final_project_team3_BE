package triplestar.mixchat.domain.member.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.domain.member.member.service.MemberService;
import triplestar.mixchat.global.response.ApiResponse;
import triplestar.mixchat.global.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/members")
public class ApiV1MemberController {

    MemberService memberService;

    @PutMapping("/me")
    public ApiResponse<Void> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody MemberInfoModifyReq memberInfoModifyReq
    ) {
        memberService.updateInfo(customUserDetails.getId(), memberInfoModifyReq);
        return ApiResponse.ok("회원 정보 수정에 성공했습니다.");
    }
}
