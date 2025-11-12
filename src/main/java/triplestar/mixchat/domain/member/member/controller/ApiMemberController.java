package triplestar.mixchat.domain.member.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.global.response.ApiResponse;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;

@Tag(name = "ApiV1MemberController", description = "API 회원 정보 관리 컨트롤러")
@CommonBadResponse
public interface ApiMemberController {

    // --- 1. 내 정보 수정 (PUT /me) ---
    @Operation(summary = "내 정보 수정", description = "인증된 사용자의 프로필 정보를 수정합니다.")
    @SignInInRequireResponse
    ApiResponse<Void> updateMyProfile(
            @Parameter(hidden = true)
            CustomUserDetails customUserDetails,
            MemberInfoModifyReq memberInfoModifyReq
    );
}
