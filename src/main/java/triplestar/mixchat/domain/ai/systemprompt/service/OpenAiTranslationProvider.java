package triplestar.mixchat.domain.ai.systemprompt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationResp;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiTranslationProvider implements TranslationProvider {

    private final OpenAiChatModel openAiChatModel;
    private final ObjectMapper objectMapper;

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

    @Override
    public Mono<TranslationResp> translate(String originalContent) {

        return Mono.defer(() -> {
            // 1) 프롬프트 구성
            Prompt prompt = new Prompt(
                    new SystemMessage(SYSTEM_PROMPT),
                    new UserMessage(originalContent)
            );

            // 2) 동기 LLM 호출
            ChatResponse response = openAiChatModel.call(prompt);

            // 3) Generation → AssistantMessage → 텍스트 추출
            String jsonResponse = response.getResult()
                    .getOutput()
                    .getText();

            log.debug("OpenAI 원본 응답 (앞 300자): {}",
                    jsonResponse.length() > 300 ? jsonResponse.substring(0, 300) + "..." : jsonResponse);

            // 4) JSON → TranslationResp 변환
            try {
                TranslationResp translation =
                        objectMapper.readValue(jsonResponse, TranslationResp.class);
                return Mono.just(translation);

            } catch (Exception e) {
                log.error("JSON 파싱 실패. 원본 응답: {}", jsonResponse, e);

                // fallback: 혹시 모를 포맷 꼬임을 JsonNode로 한 번 더 시도
                try {
                    JsonNode node = objectMapper.readTree(jsonResponse);
                    TranslationResp fallback = objectMapper.treeToValue(node, TranslationResp.class);
                    return Mono.just(fallback);
                } catch (Exception ignore) {
                    log.error("Fallback JSON 파싱도 실패했습니다.");

                    return Mono.error(new IllegalStateException(
                            "OpenAI 응답을 TranslationResp로 파싱하는 데 실패했습니다. raw=" + jsonResponse, e
                    ));
                }
            }
        });
    }

    @Override
    public int getOrder() {
        return 1; // 1순위
    }
}