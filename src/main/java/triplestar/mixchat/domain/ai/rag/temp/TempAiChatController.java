package triplestar.mixchat.domain.ai.rag.temp;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.global.response.CustomResponse;

// Temporary controller for AI chat functionality
@RestController
@RequestMapping("api/v1/ai")
@RequiredArgsConstructor
public class TempAiChatController {

    private final ChatClient ollamaChatClient;

    @PostMapping(value = "/temp/chat")
    public CustomResponse<TempAiResp> chat(@RequestBody TempAiReq req) {
        String content = ollamaChatClient.prompt()
                .user(req.message())
                .call()
                .content();

        TempAiResp resp = new TempAiResp(content);
        return CustomResponse.ok("AI chat response placeholder", resp);
    }
}