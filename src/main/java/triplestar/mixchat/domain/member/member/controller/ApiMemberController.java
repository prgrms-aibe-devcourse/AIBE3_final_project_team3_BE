package triplestar.mixchat.domain.member.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "ApiV1MemberController", description = "API 회원 정보 관리 컨트롤러")
@SuccessResponse
@CommonBadResponse
@SecurityRequirement(name = "Authorization")
public interface ApiMemberController {

    // --- 1. 내 정보 수정 (PUT /me) ---
    @Operation(summary = "내 정보 수정", description = "인증된 사용자의 프로필 정보를 수정합니다.")
    @SignInInRequireResponse
    CustomResponse<Void> updateMyProfile(
            @Parameter(hidden = true)
            CustomUserDetails customUserDetails,
            MemberInfoModifyReq memberInfoModifyReq
    );

    // --- 2. 프로필 이미지 업로드 (PUT /profile/image) ---
    @Operation(summary = "프로필 이미지 업로드", description = "인증된 사용자의 프로필 이미지를 S3에 업로드하고 URL을 DB에 저장합니다.")
    @SignInInRequireResponse
    CustomResponse<Void> uploadProfileImage(
            @Parameter(hidden = true)
            CustomUserDetails customUserDetails,
            @Parameter(description = "업로드할 이미지 파일")
            MultipartFile multipartFile
    );

    // --- 3. 내 정보 조회 (GET /me) ---
    @Operation(summary = "내 정보 조회", description = "인증된 사용자의 프로필 정보를 조회합니다.")
    @SignInInRequireResponse
    ApiResponse<triplestar.mixchat.domain.member.auth.dto.MemberSummaryResp> getMyProfile(
            @Parameter(hidden = true)
            CustomUserDetails customUserDetails
    );
}
