package triplestar.mixchat.domain.member.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.member.auth.dto.MemberJoinReq;
import triplestar.mixchat.domain.member.auth.dto.SigninReq;
import triplestar.mixchat.domain.member.auth.dto.MemberSummaryResp;
import triplestar.mixchat.domain.member.auth.dto.SignInResp;
import triplestar.mixchat.domain.member.auth.service.AuthService;
import triplestar.mixchat.global.response.ApiResponse;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class ApiV1AuthController implements ApiAuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-token-expiration-seconds}")
    private int refreshTokenExpireSeconds;

    @Value("${cookie.domain}")
    private String cookieDomain;

    @PostMapping("/join")
    public ApiResponse<MemberSummaryResp> join(
            @RequestBody @Valid MemberJoinReq memberJoinReq
    ) {
        MemberSummaryResp resp = authService.join(memberJoinReq);
        return ApiResponse.ok("회원가입에 성공했습니다.", resp);
    }

    @PostMapping("/sign-in")
    public ApiResponse<String> signIn(
            @RequestBody @Valid SigninReq signInReq,
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
        cookie.setDomain(cookieDomain);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "strict");
        cookie.setMaxAge(refreshTokenExpireSeconds);

        return cookie;
    }

    @PostMapping("/reissue")
    public ApiResponse<String> reissue(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String refreshToken = findRefreshTokenCookie(httpServletRequest);
        SignInResp resp = authService.reissueAccessToken(refreshToken);

        Cookie cookie = generateRefreshTokenCookie(resp.refreshToken());
        httpServletResponse.addCookie(cookie);

        return ApiResponse.ok("액세스 토큰이 재발급되었습니다.", resp.accessToken());
    }

    private String findRefreshTokenCookie(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null || cookies.length == 0) {
            throw new BadCredentialsException("리프레시 토큰이 존재하지 않습니다.");
        }

        return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("RefreshToken"))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("리프레시 토큰이 존재하지 않습니다."))
                .getValue();
    }
}
