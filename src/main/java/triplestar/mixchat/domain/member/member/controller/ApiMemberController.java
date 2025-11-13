package triplestar.mixchat.domain.member.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.global.response.ApiResponse;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "ApiV1MemberController", description = "API 회원 정보 관리 컨트롤러")
@SuccessResponse
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

    // --- 2. 프로필 이미지 업로드 (PUT /profile/image) ---
    @Operation(
            summary = "프로필 이미지 업로드",
            description = "인증된 사용자의 프로필 이미지를 S3에 업로드하고 URL을 DB에 저장합니다.",
            // 파일 업로드 요청을 위한 RequestBody 설정
            requestBody = @RequestBody(
                    description = "업로드할 이미지 파일 (multipart/form-data)",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(type = "object"),
                            encoding = @Encoding(name = "multipartFile", contentType = "image/*")
                    )
            )
    )
    @SignInInRequireResponse
    ApiResponse<Void> uploadProfileImage(
            @Parameter(hidden = true)
            CustomUserDetails customUserDetails,
            @Parameter(description = "업로드할 이미지 파일")
            MultipartFile multipartFile
    );
}
