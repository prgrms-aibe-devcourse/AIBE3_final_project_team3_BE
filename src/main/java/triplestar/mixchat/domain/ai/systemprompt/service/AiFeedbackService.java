package triplestar.mixchat.domain.ai.systemprompt.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.template.NoOpTemplateRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackResp;
import triplestar.mixchat.domain.chat.exception.TooManyRequestsException;

@Slf4j
@Service
public class AiFeedbackService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final OpenAiChatOptions options;

    public AiFeedbackService(
            @Qualifier("openAi") ChatClient chatClient,
            ObjectMapper baseMapper
    ) {
        this.chatClient = chatClient;
        this.objectMapper = baseMapper.copy()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // OpenAI Structured Output 스키마 고정
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .jsonSchema(ResponseFormat.JsonSchema.builder()
                        .name("AiFeedbackResp")
                        .schema(RESPONSE_SCHEMA)
                        .strict(true)
                        .build())
                .build();

        this.options = OpenAiChatOptions.builder()
                .temperature(0.1)
                .responseFormat(responseFormat)
                .build();
    }

    // feedback 항목 스키마
    private static final String FEEDBACK_ITEM_SCHEMA = """
    {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "tag": {
          "type": "string",
          "enum": ["GRAMMAR", "VOCABULARY", "TRANSLATION"]
        },
        "problem": { "type": "string", "minLength": 1 },
        "correction": { "type": "string", "minLength": 1 },
        "extra": { "type": "string", "minLength": 1 }
      },
      "required": ["tag", "problem", "correction", "extra"]
    }
    """;

    // 전체 응답 스키마
    private static final String RESPONSE_SCHEMA = """
    {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "correctedContent": {
          "type": "string",
          "minLength": 1
        },
        "feedback": {
          "type": "array",
          "items": %s
        }
      },
      "required": ["correctedContent", "feedback"]
    }
    """.formatted(FEEDBACK_ITEM_SCHEMA);

    private static final String SYSTEM_PROMPT = """
        You are an expert English tutor.

        You will receive:
        - Original: what the user typed (may include Korean or broken English).
        - Translated: an automatic English translation (use as a hint only).

        Produce:
        - correctedContent: one natural English sentence.
        - feedback: each item must represent exactly one issue.

        Classification rules:
        - TRANSLATION: Korean (or non-English) fragments.
        - GRAMMAR: tense, agreement, verb form, structure issues.
        - VOCABULARY: word choice, spelling, unnatural expressions.

        Rules:
        - extra must be written in Korean.
        - problem must be a substring of Original.
        - correction must appear in correctedContent.
        """;

    public AiFeedbackResp analyze(AiFeedbackReq req) {
        String original = normalize(req.originalContent());
        String translated = normalize(req.translatedContent());

        String userMessage = buildUserMessage(original, translated);

        try {
            String rawJson = chatClient.prompt()
                    // 프롬프트 내 중괄호 템플릿 처리 방지
                    .templateRenderer(new NoOpTemplateRenderer())
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .options(options)
                    .call()
                    .content();

            AiFeedbackResp parsed =
                    objectMapper.readValue(rawJson, AiFeedbackResp.class);

            return postValidateAndNormalize(parsed);

        } catch (Exception e) {
            boolean isRateLimit =
                    e.getMessage() != null &&
                            (e.getMessage().contains("429")
                                    || e.getMessage().contains("rate limit"));

            if (isRateLimit) {
                log.warn("OpenAI rate limit 발생: {}", e.getMessage());
                throw new TooManyRequestsException(
                        "AI 요청이 많아 분석할 수 없습니다. 잠시 후 다시 시도해주세요.", e
                );
            }

            log.error("AI 피드백 분석 실패", e);
            throw new IllegalStateException("AI 분석 중 오류가 발생했습니다.", e);
        }
    }

    private static String buildUserMessage(String original, String translated) {
        return """
            Original:
            <<<%s>>>

            Translated:
            <<<%s>>>
            """.formatted(original, translated);
    }

    private static String normalize(String s) {
        return StringUtils.hasText(s) ? s.strip() : "";
    }

    private AiFeedbackResp postValidateAndNormalize(AiFeedbackResp resp) {
        if (resp == null || !StringUtils.hasText(resp.correctedContent())) {
            throw new IllegalStateException("AI 응답이 비어 있습니다.");
        }

        List<AiFeedbackResp.FeedbackItem> items =
                resp.feedback() == null ? List.of() : resp.feedback();

        List<AiFeedbackResp.FeedbackItem> cleaned = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (AiFeedbackResp.FeedbackItem it : items) {
            if (it == null || it.tag() == null) continue;

            String problem = safe(it.problem());
            String correction = safe(it.correction());
            String extra = safe(it.extra());

            if (!StringUtils.hasText(problem)
                    || !StringUtils.hasText(correction)
                    || !StringUtils.hasText(extra)) {
                continue;
            }

            String key = (it.tag().name() + "|" + problem + "|" + correction)
                    .toLowerCase(Locale.ROOT);

            if (seen.add(key)) {
                cleaned.add(new AiFeedbackResp.FeedbackItem(
                        it.tag(), problem, correction, extra
                ));
            }
        }

        return new AiFeedbackResp(resp.correctedContent().trim(), cleaned);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}