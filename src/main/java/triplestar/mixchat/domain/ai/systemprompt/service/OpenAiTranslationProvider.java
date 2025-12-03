package triplestar.mixchat.domain.ai.systemprompt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationResp;

@Slf4j
@Component
@Order(2)
public class OpenAiTranslationProvider implements TranslationProvider {

    private final ChatClient chatClient;

    // 1. AiConfig에서 정의한 openai 빈
    public OpenAiTranslationProvider(@Qualifier("openai") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    private static final String SYSTEM_PROMPT = """
        You are a Korean-to-English translation assistant.

        TASK:
        - If the input is Korean (or mixed with Korean), translate it into natural English.
        - If the input is already English, fix obvious grammar/wording mistakes and output the improved English.
        - If the input is already perfect English, return it as-is.

        OUTPUT RULES:
        - Output ONLY the final English sentence.
        - Do NOT add explanations, labels, quotes, or JSON.
        - Just return the translated/corrected sentence itself.
        """;

    @Override
    public TranslationResp translate(String originalContent) {
        try {
            String translatedText = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(originalContent)
                    .call()
                    .content();

            if (translatedText == null || translatedText.isBlank()) {
                log.warn("OpenAI 응답이 비어있음 -> Fallback");
                return null;
            }

            return new TranslationResp(originalContent, translatedText.trim());

        } catch (Exception e) {
            log.error("OpenAI 번역 실패: {}", e.getMessage());
            return null;
        }
    }
}