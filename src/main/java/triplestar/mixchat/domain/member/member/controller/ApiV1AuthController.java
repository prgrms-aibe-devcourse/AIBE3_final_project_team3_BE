package triplestar.mixchat.domain.member.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "ApiV1AuthController", description = "API 인증/인가 컨트롤러")
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class ApiV1AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 회원으로 가입시킵니다.")
    @PostMapping("/join")
    public ApiResponse<MemberSummaryResp> join(
            @RequestBody @Valid MemberJoinReq memberJoinReq
    ) {
        MemberSummaryResp resp = authService.join(memberJoinReq);
        return ApiResponse.ok("회원가입에 성공했습니다.", resp);
    }
}
