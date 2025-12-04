package triplestar.mixchat.domain.ai.systemprompt.service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationResp;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class OllamaTranslationProvider implements TranslationProvider {

    private final WebClient.Builder webClientBuilder;

    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.ollama.api-key:}")
    private String apiKey;

    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        WebClient.Builder builder = webClientBuilder.baseUrl(ollamaBaseUrl);

        // API Key가 있으면 Bearer 토큰으로 추가
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + apiKey);
        }

        this.webClient = builder.build();
    }

    private record ChatRequest(String model, List<Message> messages, boolean stream) {}
    private record Message(String role, String content) {}
    private record ChatResponse(Message message) {}

    @Override
    public TranslationResp translate(String originalContent) {
        try {
            ChatRequest request = new ChatRequest(
                    model,
                    List.of(
                            new Message("system", SYSTEM_PROMPT),
                            new Message("user", originalContent)
                    ),
                    false
            );

            Mono<ChatResponse> responseMono = webClient.post()
                    .uri("/api/chat")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatResponse.class);

            ChatResponse response = responseMono.block();

            if (response == null || response.message() == null || response.message().content() == null) {
                log.warn("Ollama 응답이 비어있음 -> Fallback");
                return null;
            }

            String translatedText = response.message().content().trim();
            return new TranslationResp(originalContent, translatedText);

        } catch (Exception e) {
            log.error("Ollama 번역 오류: {}", e.getMessage());
            return null;
        }
    }
}