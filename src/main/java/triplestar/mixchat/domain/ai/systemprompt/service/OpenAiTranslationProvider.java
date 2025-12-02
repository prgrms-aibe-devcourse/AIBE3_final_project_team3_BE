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
        You are Mixchat's English tutor. Analyze the user's sentence and respond ONLY in a single JSON object.

        RULES:
        1. If the user's sentence is already a natural and correct English sentence,
           respond with a JSON where "corrected_content" is null.
        2. If the sentence is Korean, mixed, or unnatural English,
           translate and correct it into ONE natural English sentence.
        3. Provide feedback items: tag, problem, correction, extra.
        4. The output MUST be a raw JSON object WITHOUT any extra text.

        JSON Structure Example:
        {
          "original_content": "...",
          "corrected_content": "... or null",
          "feedback": [
            {
              "tag": "GRAMMAR",
              "problem": "was go",
              "correction": "went",
              "extra": "Past tense must be used here..."
            }
          ]
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