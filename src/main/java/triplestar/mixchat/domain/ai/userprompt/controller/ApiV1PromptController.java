package triplestar.mixchat.domain.ai.userprompt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import triplestar.mixchat.domain.ai.userprompt.dto.UserPromptReq;
import triplestar.mixchat.domain.ai.userprompt.dto.UserPromptResp;
import triplestar.mixchat.domain.ai.userprompt.dto.UserPromptDetailResp;
import triplestar.mixchat.domain.ai.userprompt.service.UserPromptService;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;
import java.util.List;

@RestController
@RequestMapping("/api/v1/prompt")
@RequiredArgsConstructor
public class ApiV1PromptController implements ApiPromptController {
    private final UserPromptService userPromptService;

    @Override
    @PostMapping("/create")
    public CustomResponse<Void> create(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody UserPromptReq req) {
        Long memberId = userDetails.getId();
        userPromptService.create(memberId, req);
        return CustomResponse.ok("프롬프트가 생성되었습니다.");
    }

    @Override
    @PutMapping("/{id}")
    public CustomResponse<Void> update(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long id,
                                       @RequestBody UserPromptReq req) {
        Long memberId = userDetails.getId();
        userPromptService.update(memberId, id, req);
        return CustomResponse.ok("프롬프트가 수정되었습니다.");
    }

    @Override
    @DeleteMapping("/{id}")
    public CustomResponse<Void> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long id) {
        Long memberId = userDetails.getId();
        userPromptService.delete(memberId, id);
        return CustomResponse.ok("프롬프트가 삭제되었습니다.");
    }

    @Override
    @GetMapping()
    public CustomResponse<List<UserPromptResp>> list(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        List<UserPromptResp> resp = userPromptService.list(memberId);
        return CustomResponse.ok("프롬프트 목록 조회 성공", resp);
    }

    @Override
    @GetMapping("/{id}")
    public CustomResponse<UserPromptDetailResp> detail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @PathVariable Long id) {
        Long memberId = userDetails.getId();
        UserPromptDetailResp resp = userPromptService.detail(memberId, id);
        return CustomResponse.ok("프롬프트 상세 조회 성공", resp);
    }
}
