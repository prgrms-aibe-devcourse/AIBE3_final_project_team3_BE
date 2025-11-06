package triplestar.mixchat.domain.member.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.member.member.dto.MemberJoinReq;
import triplestar.mixchat.domain.member.member.dto.MemberSummaryResp;
import triplestar.mixchat.domain.member.member.service.AuthService;
import triplestar.mixchat.global.response.ApiResponse;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class ApiV1AuthController {

    private final AuthService authService;

    @PostMapping("/join")
    public ApiResponse<MemberSummaryResp> join(
            @RequestBody @Valid MemberJoinReq memberJoinReq
    ) {
        MemberSummaryResp resp = authService.join(memberJoinReq);
        return ApiResponse.ok("회원가입에 성공했습니다.", resp);
    }
}
