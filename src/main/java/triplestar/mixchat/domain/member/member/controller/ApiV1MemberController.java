package triplestar.mixchat.domain.member.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.domain.member.member.service.MemberService;
import triplestar.mixchat.global.response.ApiResponse;
import triplestar.mixchat.global.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController implements ApiMemberController {

    private final MemberService memberService;

    @Override
    @PutMapping("/profile")
    public ApiResponse<Void> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody MemberInfoModifyReq memberInfoModifyReq
    ) {
        memberService.updateInfo(customUserDetails.getId(), memberInfoModifyReq);
        return ApiResponse.ok("회원 정보 수정에 성공했습니다.");
    }

    @Override
    @PutMapping("/profile/image")
    public ApiResponse<Void> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestPart MultipartFile multipartFile
    ) {
        memberService.uploadProfileImage(customUserDetails.getId(), multipartFile);
        return ApiResponse.ok("프로필 이미지 업로드에 성공했습니다.");
    }
}
