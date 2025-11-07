package triplestar.mixchat.domain.member.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.member.member.dto.MemberJoinReq;
import triplestar.mixchat.domain.member.member.dto.MemberSignInReq;
import triplestar.mixchat.domain.member.member.dto.MemberSummaryResp;
import triplestar.mixchat.domain.member.member.dto.SignInResp;
import triplestar.mixchat.domain.member.member.service.AuthService;
import triplestar.mixchat.global.response.ApiResponse;

@RestController
@Tag(name = "ApiV1AuthController", description = "API 인증/인가 컨트롤러")
@RequestMapping("api/v1/auth")
public class ApiV1AuthController {

    private final AuthService authService;
    private final int refreshTokenExpireSeconds;

    public ApiV1AuthController(
            AuthService authService,
            @Value("${jwt.refresh-token-expiration-seconds}")
            int refreshTokenExpireSeconds
    ) {
        this.authService = authService;
        this.refreshTokenExpireSeconds = refreshTokenExpireSeconds;
    }

    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "새로운 사용자를 회원으로 가입시킵니다.")
    public ApiResponse<MemberSummaryResp> join(
            @RequestBody @Valid MemberJoinReq memberJoinReq
    ) {
        MemberSummaryResp resp = authService.join(memberJoinReq);
        return ApiResponse.ok("회원가입에 성공했습니다.", resp);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증을 수행하고 토큰을 발급합니다.")
    public ApiResponse<String> signIn(
            @RequestBody @Valid MemberSignInReq signInReq,
            HttpServletResponse httpServletResponse
    ) {
        SignInResp resp = authService.signIn(signInReq);

        Cookie cookie = generateRefreshTokenCookie(resp.refreshToken());
        httpServletResponse.addCookie(cookie);

        return ApiResponse.ok("로그인에 성공했습니다.", resp.accessToken());
    }

    private Cookie generateRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("RefreshToken", refreshToken);

        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "strict");
        cookie.setMaxAge(refreshTokenExpireSeconds);

        return cookie;
    }
}
