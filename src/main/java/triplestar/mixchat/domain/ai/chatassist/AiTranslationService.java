package triplestar.mixchat.domain.ai.chatassist;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.systemprompt.constant.PromptKey;
import triplestar.mixchat.domain.ai.systemprompt.dto.TempAiReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.TempAiResp;
import triplestar.mixchat.domain.ai.systemprompt.entity.SystemPrompt;
import triplestar.mixchat.domain.ai.systemprompt.service.SystemPromptService;

@Service
@RequiredArgsConstructor
public class AiTranslationService {

    private final ChatClient chatClient;
    private final SystemPromptService systemPromptService;

    public TempAiResp sendMessage(TempAiReq req) {
        SystemPrompt systemPrompt = systemPromptService.getLatestByKey(PromptKey.AI_ASSIST);
        String template = systemPrompt.getContent();

        String prompt = template.replace("{{input}}", req.message());
        String call = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

        // TODO : 응답 파싱 및 검증 로직 추가 필요
        // TODO : Strict JSON Mode 설정, Function Calling 활용 등

        return new TempAiResp(call);
    }
}
