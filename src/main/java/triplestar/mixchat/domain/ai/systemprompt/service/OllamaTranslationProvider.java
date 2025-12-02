package triplestar.mixchat.domain.ai.systemprompt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
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
@RequiredArgsConstructor
@Order(1)
public class OllamaTranslationProvider implements TranslationProvider {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${ollama.api.url}")
    private String ollamaApiUrl;

    @Value("${ollama.api.model}")
    private String ollamaModel;

    @Value("${ollama.api.key}")
    private String apiKey;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder
                .baseUrl(ollamaApiUrl)
                .defaultHeaders(headers -> headers.setBearerAuth(apiKey))
                .build();
    }

    private record OllamaRequest(String model, String prompt, boolean stream, String format) {}
    private record OllamaResponse(String response) {}

    private static final String SYSTEM_PROMPT = """
        Translate Korean to English. Correct broken English. Output JSON only.

        Format: {"original_content":"input","corrected_content":"translation or null","feedback":[{"tag":"TRANSLATION|GRAMMAR","problem":"word","correction":"fix","extra":"reason"}]}

        Rules: Korean→English always. Perfect English→null. Broken English→fix.
        """;

    @Override
    public Mono<TranslationResp> translate(String originalContent) {
        // 시스템 프롬프트에 사용자 입력 주입 (Ollama Native API 방식은 프롬프트가 하나)
        // Chat 포맷을 흉내내기 위해 텍스트로 합침
        String fullPrompt = SYSTEM_PROMPT + "\n\nUser input:\n" + originalContent;

        OllamaRequest ollamaRequest = new OllamaRequest(ollamaModel, fullPrompt, false, "json");

        return webClient.post()
                .uri("/api/generate")
                .bodyValue(ollamaRequest)
                .retrieve()
                .bodyToMono(OllamaResponse.class)
                .map(ollamaResponse -> {
                    try {
                        return objectMapper.readValue(ollamaResponse.response(), TranslationResp.class);
                    } catch (IOException e) {
                        log.error("Ollama 응답 JSON 파싱 실패: {}", ollamaResponse.response(), e);
                        throw new RuntimeException("Failed to parse Ollama response", e);
                    }
                })
                .doOnError(e -> log.error("Ollama 번역 중 오류 발생: {}", e.getMessage(), e));
    }
}