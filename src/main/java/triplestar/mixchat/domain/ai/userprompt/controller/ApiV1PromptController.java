package triplestar.mixchat.domain.ai.userprompt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import triplestar.mixchat.domain.ai.userprompt.dto.PromptReq;
import triplestar.mixchat.domain.ai.userprompt.dto.PromptListResp;
import triplestar.mixchat.domain.ai.userprompt.dto.PromptDetailResp;
import triplestar.mixchat.domain.ai.userprompt.service.PromptService;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;
import java.util.List;

@RestController
@RequestMapping("/api/v1/prompt")
@RequiredArgsConstructor
public class ApiV1PromptController implements ApiPromptController {
    private final PromptService promptService;

    @Override
    @PostMapping("/create")
    public CustomResponse<Void> create(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody PromptReq req) {
        Long memberId = userDetails.getId();
        promptService.create(memberId, req);
        return CustomResponse.ok("프롬프트가 생성되었습니다.");
    }

    @Override
    @PutMapping("/{id}")
    public CustomResponse<Void> update(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long id,
                                       @RequestBody PromptReq req) {
        Long memberId = userDetails.getId();
        promptService.update(memberId, id, req);
        return CustomResponse.ok("프롬프트가 수정되었습니다.");
    }

    @Override
    @DeleteMapping("/{id}")
    public CustomResponse<Void> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long id) {
        Long memberId = userDetails.getId();
        promptService.delete(memberId, id);
        return CustomResponse.ok("프롬프트가 삭제되었습니다.");
    }

    @Override
    @GetMapping()
    public CustomResponse<List<PromptListResp>> list(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        List<PromptListResp> resp = promptService.list(memberId);
        return CustomResponse.ok("프롬프트 목록 조회 성공", resp);
    }

    @Override
    @GetMapping("/{id}")
    public CustomResponse<PromptDetailResp> detail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @PathVariable Long id) {
        Long memberId = userDetails.getId();
        PromptDetailResp resp = promptService.detail(memberId, id);
        return CustomResponse.ok("프롬프트 상세 조회 성공", resp);
    }
}
