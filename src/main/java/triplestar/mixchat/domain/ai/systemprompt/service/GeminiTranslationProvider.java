package triplestar.mixchat.domain.ai.systemprompt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationResp;

@Slf4j
// @Component // Vertex AI 설정 필요 - 당장 사용하지 않으므로 비활성화
@Order(3) // 가장 마지막(Ollama -> OpenAI -> Gemini)
public class GeminiTranslationProvider implements TranslationProvider {

    private final ChatClient chatClient;

    // 1. AiConfig에서 설정한 gemini 빈
    public GeminiTranslationProvider(@Qualifier("gemini") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public TranslationResp translate(String originalContent) {
        try {
            String translatedText = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(originalContent)
                    .call()
                    .content();

            if (translatedText == null || translatedText.isBlank()) {
                log.warn("Gemini 응답이 비어있음 -> Fallback");
                return null;
            }

            return new TranslationResp(originalContent, translatedText.trim());

        } catch (Exception e) {
            log.error("Gemini 번역 실패: {}", e.getMessage());
            return null;
        }
    }
}