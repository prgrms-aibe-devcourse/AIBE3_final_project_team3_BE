package triplestar.mixchat.domain.member.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.member.auth.dto.SignUpReq;
import triplestar.mixchat.domain.member.member.dto.MemberPresenceSummaryResp;
import triplestar.mixchat.domain.member.auth.dto.LogInResp;
import triplestar.mixchat.domain.member.auth.dto.LogInReq;
import triplestar.mixchat.domain.member.auth.service.AuthService;
import triplestar.mixchat.domain.member.auth.util.CookieHelper;
import triplestar.mixchat.domain.member.member.dto.MemberSummaryResp;
import triplestar.mixchat.global.response.CustomResponse;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class ApiV1AuthController implements ApiAuthController {

    private final AuthService authService;
    private final CookieHelper cookieHelper;

    @Override
    @PostMapping("/join")
    public CustomResponse<MemberSummaryResp> join(
            @RequestBody @Valid SignUpReq signUpReq
    ) {
        MemberSummaryResp resp = authService.join(signUpReq);
        return CustomResponse.ok("회원가입에 성공했습니다.", resp);
    }

    @Override
    @PostMapping("/login")
    public CustomResponse<String> login(
            @RequestBody @Valid LogInReq logInReq,
            HttpServletResponse httpServletResponse
    ) {
        LogInResp resp = authService.login(logInReq);

        Cookie cookie = cookieHelper.generateRefreshTokenCookie(resp.refreshToken());
        httpServletResponse.addCookie(cookie);

        return CustomResponse.ok("로그인에 성공했습니다.", resp.accessToken());
    }

    @Override
    @PostMapping("/reissue")
    public CustomResponse<String> reissue(HttpServletRequest httpServletRequest,
                                          HttpServletResponse httpServletResponse) {
        String refreshToken = cookieHelper.findRefreshTokenCookie(httpServletRequest);
        LogInResp resp = authService.reissueAccessToken(refreshToken);

        Cookie cookie = cookieHelper.generateRefreshTokenCookie(resp.refreshToken());
        httpServletResponse.addCookie(cookie);

        return CustomResponse.ok("액세스 토큰이 재발급되었습니다.", resp.accessToken());
    }

    @Override
    @PostMapping("/logout")
    public CustomResponse<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (request.getCookies() == null) {
            return CustomResponse.ok("로그아웃 되었습니다.");
        }

        String refreshToken = cookieHelper.findRefreshTokenCookie(request);

        authService.logout(refreshToken);
        response.addCookie(cookieHelper.generateExpiredRefreshToken());

        return CustomResponse.ok("로그아웃 되었습니다.");
    }
}
