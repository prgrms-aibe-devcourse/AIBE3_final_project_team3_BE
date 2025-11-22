package triplestar.mixchat.domain.member.member.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.chat.find.service.FindService;
import triplestar.mixchat.domain.member.member.dto.MemberDetailResp;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.domain.member.member.dto.MemberSummaryResp;
import triplestar.mixchat.domain.member.member.service.MemberService;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController implements ApiMemberController {

    private final MemberService memberService;

    @Override
    @PutMapping("/profile")
    public CustomResponse<Void> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody MemberInfoModifyReq memberInfoModifyReq
    ) {
        memberService.updateInfo(customUserDetails.getId(), memberInfoModifyReq);
        return CustomResponse.ok("회원 정보 수정에 성공했습니다.");
    }

    @Override
    @PutMapping("/profile/image")
    public CustomResponse<Void> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestPart MultipartFile multipartFile
    ) {
        memberService.uploadProfileImage(customUserDetails.getId(), multipartFile);
        return CustomResponse.ok("프로필 이미지 업로드에 성공했습니다.");
    }

    @Override
    @GetMapping("/me")
    public CustomResponse<MemberDetailResp> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        MemberDetailResp memberProfile = memberService.getMemberDetails(customUserDetails.getId(),
                customUserDetails.getId());
        return CustomResponse.ok("내 정보를 성공적으로 조회했습니다.", memberProfile);
    }

    @Override
    @GetMapping("/{id}")
    public CustomResponse<MemberDetailResp> getMemberDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long id
    ) {
        Long signInId = customUserDetails != null ? customUserDetails.getId() : null;

        MemberDetailResp memberDetails = memberService.getMemberDetails(signInId, id);
        return CustomResponse.ok("회원 상세 정보 조회에 성공했습니다.", memberDetails);
    }

    // TODO : 온라인 유저 표시
    @Override
    @GetMapping
    public CustomResponse<Page<MemberSummaryResp>> getMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = userDetails.getId() != null ? userDetails.getId() : -1L;
        Page<MemberSummaryResp> members = memberService.findAllMembers(userId, pageable);
        return CustomResponse.ok("모든 회원 목록을 성공적으로 조회했습니다.", members);
    }

    @Override
    @DeleteMapping("/me")
    public CustomResponse<Void> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        memberService.deleteSoftly(customUserDetails.getId());
        return CustomResponse.ok("회원 탈퇴에 성공했습니다.");
    }
}
