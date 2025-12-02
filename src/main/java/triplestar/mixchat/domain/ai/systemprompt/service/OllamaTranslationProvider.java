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

    @Override
    public Mono<TranslationResp> translate(String originalContent) {
        String systemPrompt = """
            You are Mixchat's English tutor. Analyze the user's sentence and respond in JSON format with translation, tags, corrections, and explanations.
            
            RULES:
            1. If the user's sentence is already a natural and grammatically correct English sentence, respond with a JSON where "corrected_content" is null.
            2. If the sentence is in Korean, a mix of languages, or is unnatural/incorrect English, translate and correct it into a single, natural English sentence.
            3. Provide feedback for each issue found, including tag, problem, correction, and extra explanation.
            4. The output format MUST be a single raw JSON object. Do not include any other text or markdown.
            
            JSON Structure:
            {
              "original_content": (The user's original input string),
              "corrected_content": (The corrected, natural English sentence. Null if no changes are needed.),
              "feedback": [
                {
                  "tag": (Issue type: GRAMMAR, VOCABULARY, TRANSLATION),
                  "problem": (The problematic word/phrase),
                  "correction": (The corrected word/phrase),
                  "extra": (Explanation of the correction)
                }, ...
              ]
            }
            
            User input:
            {input}
            """;

        String prompt = systemPrompt.replace("{input}", originalContent);
        OllamaRequest ollamaRequest = new OllamaRequest(ollamaModel, prompt, false, "json");

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
                });
    }
}