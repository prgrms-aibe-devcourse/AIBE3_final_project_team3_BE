package triplestar.mixchat.domain.member.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.chat.find.service.FindService;
import triplestar.mixchat.domain.member.auth.dto.MemberSummaryResp;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.domain.member.member.service.MemberService;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController implements ApiMemberController {

    private final MemberService memberService;
    private final FindService findService;

    @Operation(summary = "모든 회원 목록 조회", description = "채팅 상대로 추가할 수 있는 모든 회원 목록을 조회합니다. 자기 자신은 목록에서 제외됩니다.")
    @GetMapping
    public CustomResponse<List<MemberSummaryResp>> findAllMembers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MemberSummaryResp> members = findService.findAllMembers(userDetails.getId());
        return CustomResponse.ok("모든 회원 목록을 성공적으로 조회했습니다.", members);
    }

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
}
