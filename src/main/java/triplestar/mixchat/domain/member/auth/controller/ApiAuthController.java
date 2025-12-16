package triplestar.mixchat.domain.member.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import triplestar.mixchat.domain.member.auth.dto.SignUpReq;
import triplestar.mixchat.domain.member.member.dto.MemberPresenceSummaryResp;
import triplestar.mixchat.domain.member.auth.dto.LogInReq;
import triplestar.mixchat.domain.member.member.dto.MemberSummaryResp;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "ApiV1AuthController", description = "API 인증/인가 컨트롤러")
@CommonBadResponse
@SuccessResponse
public interface ApiAuthController {

    // --- 1. 회원가입 (POST /join) ---
    @Operation(summary = "회원가입", description = "새로운 사용자를 회원으로 가입시킵니다.")
    CustomResponse<MemberSummaryResp> join(
            @RequestBody(description = "가입 정보", required = true)
            SignUpReq signUpReq
    );

    // --- 2. 로그인 (POST /sign-in) ---
    @Operation(summary = "로그인", description = "사용자 인증을 수행하고 토큰을 발급합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "404", description = "존재하지 않는 사용자 (MEMBER_NOT_FOUND)",
                            content = @Content(schema = @Schema(implementation = CustomResponse.class))
                    )
            }
    )
    CustomResponse<String> login(
            @RequestBody(description = "로그인 정보", required = true)
            LogInReq logInReq,
            HttpServletResponse httpServletResponse
    );

    // --- 3. 토큰 재발급 (POST /reissue) ---
    @Operation(
            summary = "토큰 재발급",
            description = "만료된 액세스 토큰을 리프레시 토큰을 통해 재발급합니다."
    )
    CustomResponse<String> reissue(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);

    // --- 4. 로그아웃 (POST /logout) ---
    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 무효화하고 클라이언트 쿠키를 만료시킵니다."
    )
    CustomResponse<Void> logout(HttpServletRequest request, HttpServletResponse response);
}