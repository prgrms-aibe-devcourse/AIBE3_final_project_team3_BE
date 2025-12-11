package triplestar.mixchat.domain.ai.systemprompt.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackResp;

@Slf4j
@Service
public class AiFeedbackService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    private static final OpenAiChatOptions FEEDBACK_OPTIONS = OpenAiChatOptions.builder()
            .responseFormat(ResponseFormat.builder().type(ResponseFormat.Type.JSON_OBJECT).build())
            .temperature(0.1)
            .build();

    // OpenAI 사용 (성능 및 JSON 포맷 준수 우수)
    public AiFeedbackService(@Qualifier("openAi") ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
        You are an expert English language tutor.
        Analyze the user's original sentence and the translated (or intended) sentence.

        INPUT:
        - Original: The sentence written by the user (may contain Korean or errors).
        - Translated: The meaning the user intended to convey.

        OUTPUT FORMAT (JSON):
        {
          "correctedContent": "Corrected English sentence",
          "feedback": [
            {
              "tag": "GRAMMAR" | "VOCABULARY" | "TRANSLATION",
              "problem": "...",
              "correction": "...",
              "extra": "..."   // 설명은 반드시 한국어로 작성
            }
          ]
        }

        RULES:
        - The 'extra' explanation should be written in Korean.
        - Respond ONLY with the JSON object.
        - For each difference, output one feedback item.
        - tag must be exactly one of: GRAMMAR, VOCABULARY, TRANSLATION (uppercase only).
        - Each feedback item must cover exactly one issue. Do NOT merge translation and grammar in one item; split them.
        - Do NOT wrap the response in Markdown fences.
        
        Example:
        
        Original: "i eat 빵 tommorow, it will have to be delicious!!"
        Translated: "I will eat bread tomorrow, and it has to be delicious!!"
        
        Expected JSON output:
        {
          "correctedContent": "I will eat bread tomorrow, and it has to be delicious!",
          "feedback": [
            {
              "tag": "GRAMMAR",
              "problem": "i eat",
              "correction": "I will eat",
              "extra": "'eat'은 현재형이며 미래 의미를 나타내기 위해 'will eat'으로 수정해야 합니다."
            },
            {
              "tag": "TRANSLATION",
              "problem": "빵",
              "correction": "bread",
              "extra": "빵 -> bread"
            },
            {
              "tag": "GRAMMAR",
              "problem": "have to",
              "correction": "has to",
              "extra": "'it'은 3인칭 단수이므로 'has to'로 수정해야 문법적으로 맞습니다."
            },
            {
              "tag": "VOCABULARY",
              "problem": "tommorow",
              "correction": "tomorrow",
              "extra": "철자 오류가 있어 'tomorrow'로 수정해야 합니다."
            }
          ]
        }

        Additional example (ensure splitting translation vs grammar):
        Original: "everyone, what be you doing 내일"
        Translated: "What are you up to tonight"
        Expected JSON output:
        {
          "correctedContent": "What are you doing tonight",
          "feedback": [
            {
              "tag": "TRANSLATION",
              "problem": "내일",
              "correction": "tonight",
              "extra": "'내일'은 의도된 의미에 맞게 'tonight'으로 번역해야 합니다."
            },
            {
              "tag": "GRAMMAR",
              "problem": "what be you doing",
              "correction": "what are you doing",
              "extra": "be 동사는 주어에 맞춰 'are'로 바꿔야 자연스러운 문장입니다."
            }
          ]
        }
        """;

    public AiFeedbackResp analyze(AiFeedbackReq req) {
        String userMessage = String.format("Original: %s%nTranslated: %s", req.originalContent(), req.translatedContent());
        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String response = chatClient.prompt()
                        .system(SYSTEM_PROMPT)
                        .user(userMessage)
                        .options(FEEDBACK_OPTIONS)
                        .call()
                        .content();

                if (response == null || response.isBlank()) {
                    throw new IllegalStateException("AI 응답이 비어있습니다.");
                }

                return parseResponse(response);

            } catch (Exception e) {
                log.error("AI 분석 실패 (시도 {}/{}): {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(200L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("AI 분석 재시도 중 인터럽트 발생", ie);
                    }
                }
                if (attempt == maxRetries) {
                    throw new IllegalStateException("AI 분석에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
                }
            }
        }

        throw new IllegalStateException("AI 분석에 실패했습니다.");
    }

    private AiFeedbackResp parseResponse(String response) throws Exception {
        String json = stripMarkdownFence(response);
        ObjectMapper safeMapper = objectMapper.copy()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return safeMapper.readValue(json, AiFeedbackResp.class);
    }

    private String stripMarkdownFence(String raw) {
        if (raw == null) {
            return "";
        }

        String trimmed = raw.trim();

        if (trimmed.startsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            if (firstNewLine > 0) {
                trimmed = trimmed.substring(firstNewLine + 1);
            } else {
                trimmed = trimmed.substring(3);
            }
        }

        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.lastIndexOf("```"));
        }

        return trimmed.trim();
    }
}
