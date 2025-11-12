package triplestar.mixchat.domain.prompt.prompt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptReq;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptListResp;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptDetailResp;
import triplestar.mixchat.domain.prompt.prompt.service.PromptService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/prompt")
@RequiredArgsConstructor
@Tag(name = "Prompt", description = "프롬프트 관련 API")
public class ApiV1PromptController {
    private final PromptService promptService;

    @Operation(summary = "프롬프트 생성", description = "새로운 커스텀 프롬프트를 DB에 저장")
    @PostMapping("/create")
    public ResponseEntity<PromptDetailResp> create(@RequestBody PromptReq req) {
        return ResponseEntity.ok(promptService.create(req));
    }

    @Operation(summary = "프롬프트 수정", description = "프리미엄 회원이 커스텀 프롬프트 수정")
    @PutMapping("/update/{id}")
    public ResponseEntity<PromptDetailResp> update(
            @Parameter(description = "프롬프트 ID") @PathVariable Long id,
            @RequestBody PromptReq req) {
        return ResponseEntity.ok(promptService.update(id, req));
    }

    @Operation(summary = "프롬프트 삭제", description = "프리미엄 회원이 본인이 생성한 커스텀 프롬프트 삭제")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "프롬프트 ID") @PathVariable Long id) {
        promptService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프롬프트 목록 조회", description = "사용 가능한 프롬프트 목록을 id와 제목만 조회")
    @GetMapping()
    public ResponseEntity<List<PromptListResp>> list() {
        return ResponseEntity.ok(promptService.list());
    }

    @Operation(summary = "프롬프트 상세 조회", description = "프롬프트 상세 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<PromptDetailResp> detail(@Parameter(description = "프롬프트 ID") @PathVariable Long id) {
        return ResponseEntity.ok(promptService.detail(id));
    }
}
