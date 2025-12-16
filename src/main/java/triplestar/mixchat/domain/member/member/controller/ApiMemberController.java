package triplestar.mixchat.domain.member.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.member.member.dto.MemberDetailResp;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.domain.member.member.dto.MemberPresenceSummaryResp;
import triplestar.mixchat.domain.member.member.dto.MyProfileResp;
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
  
    // --- 3. 회원 상세 조회 (GET /{id}) ---
    @Operation(
            summary = "회원 상세 조회",
            description = "특정 회원의 상세 정보를 조회합니다. 토큰이 없거나 유효하지 않아도 조회 가능합니다. 로그인된 경우 친구/요청 상태 정보가 추가됩니다."
    )
    CustomResponse<MemberDetailResp> getMemberDetail(
            @Parameter(hidden = true)
            CustomUserDetails customUserDetails, // 토큰이 있다면 인증 정보를 주입
            @Parameter(description = "조회 대상 회원의 ID", example = "10")
            Long id
    );

    // --- 4. 내 정보 조회 (GET /me) ---
    @Operation(summary = "내 정보 조회", description = "인증된 사용자의 상세 정보를 조회합니다.")
    @SignInInRequireResponse
    CustomResponse<MyProfileResp> getMyProfile(
            @Parameter(hidden = true) CustomUserDetails customUserDetails
    );

    // --- 5. 모든 회원 목록 조회 (GET /) ---
    @Operation(summary = "모든 회원 목록 조회", description = "채팅 상대로 추가할 수 있는 모든 회원 목록을 조회합니다. 자기 자신은 목록에서 제외됩니다.")
    CustomResponse<Page<MemberPresenceSummaryResp>> getMembers(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            @Parameter(description = "페이지 정보")
            Pageable pageable
    );

    // --- 6. 온라인 회원 목록 조회 (GET /online) ---
    @Operation(summary = "온라인 회원 목록 조회",
            description = "현재 Heartbeat를 보내 온라인 상태로 확인된 회원 목록을 페이지로 조회합니다. 로그인 여부와 무관하게 조회 가능합니다.")
    CustomResponse<Page<MemberPresenceSummaryResp>> getOnlineMembers(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            @Parameter(description = "페이지 정보 (size=20 기본값)")
            Pageable pageable
    );

    // --- 7. 회원 탈퇴 (DELETE /me) ---
    @Operation(summary = "회원 탈퇴 (Soft Delete)",
            description = "현재 로그인된 사용자의 계정을 비활성화(Soft Delete) 처리합니다.")
    @SignInInRequireResponse
    CustomResponse<Void> deleteMyAccount(
            @Parameter(hidden = true)
            CustomUserDetails customUserDetails
    );
}
