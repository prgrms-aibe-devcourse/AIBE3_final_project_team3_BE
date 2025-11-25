package triplestar.mixchat.domain.ai.systemprompt;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiTranslationReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiTranslationResp;
import triplestar.mixchat.domain.ai.systemprompt.service.AiTranslationService;
import triplestar.mixchat.global.response.CustomResponse;

// Temporary controller for AI chat functionality
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class TempAiChatController {

    private final AiTranslationService aiTranslationService;

    @PostMapping("/chat")
    public CustomResponse<AiTranslationResp> chat (@RequestBody AiTranslationReq req) {
        AiTranslationResp resp = aiTranslationService.sendMessage(req.message());
        return CustomResponse.ok("AI chat response placeholder", resp);
    }
}
