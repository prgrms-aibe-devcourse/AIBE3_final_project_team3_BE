package triplestar.mixchat.domain.ai.systemprompt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationResp;

@Slf4j
@Component
@Order(2)
public class OpenAiTranslationProvider implements TranslationProvider {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
        You are Mixchat's English translation tutor. Your job is to translate Korean to English and correct English grammar.

        RULES:
        1. If input is in Korean (or contains Korean), ALWAYS translate to natural English and set "corrected_content".
        2. If input is already perfect English, set "corrected_content" to null.
        3. If input is broken/unnatural English, correct it and set "corrected_content".
        4. Provide feedback for issues (GRAMMAR, VOCABULARY, TRANSLATION).
        5. Output MUST be a single raw JSON object. NO extra text.

        JSON Structure Example:
        {
          "original_content": "안녕하세요",
          "corrected_content": "Hello",
          "feedback": [
            {
              "tag": "TRANSLATION",
              "problem": "안녕하세요",
              "correction": "Hello",
              "extra": "Korean greeting translated to English"
            }
          ]
        }

        Example 2:
        {
          "original_content": "I am go to school",
          "corrected_content": "I am going to school",
          "feedback": [
            {
              "tag": "GRAMMAR",
              "problem": "am go",
              "correction": "am going",
              "extra": "Present continuous tense required"
            }
          ]
        }

        Example 3:
        {
          "original_content": "Hello, how are you?",
          "corrected_content": null,
          "feedback": []
        }
        """;

    public OpenAiTranslationProvider(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Override
    public Mono<TranslationResp> translate(String originalContent) {
        return Mono.fromCallable(() -> chatClient.prompt()
                        .user(originalContent)
                        .call()
                        .entity(TranslationResp.class)
                )
                .subscribeOn(Schedulers.boundedElastic()) // Blocking I/O 처리를 위해 별도 스레드 풀 사용
                .doOnError(e -> log.error("OpenAI 번역 중 오류 발생: {}", e.getMessage(), e));
    }
}