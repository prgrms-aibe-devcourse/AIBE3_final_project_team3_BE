package triplestar.mixchat.domain.chat.find.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.chat.find.service.FindService;
import triplestar.mixchat.domain.member.auth.dto.MemberSummaryResp;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;

import java.util.List;

@Tag(name = "Find API", description = "채팅 상대를 찾기 위한 API")
@RestController
@RequestMapping("/api/v1/find")
@RequiredArgsConstructor
public class ApiV1FindController {

    private final FindService findService;

    @Operation(summary = "모든 회원 목록 조회", description = "채팅 상대로 추가할 수 있는 모든 회원 목록을 조회합니다. 자기 자신은 목록에서 제외됩니다.")
    @GetMapping("/members")
    public CustomResponse<List<MemberSummaryResp>> findAllMembers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MemberSummaryResp> members = findService.findAllMembers(userDetails.getId());
        return CustomResponse.ok("모든 회원 목록을 성공적으로 조회했습니다.", members);
    }
}
