package triplestar.mixchat.domain.ai.systemprompt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackResp;

@Slf4j
@Service
public class AiFeedbackService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    // OpenAI 사용 (성능 및 JSON 포맷 준수 우수)
    public AiFeedbackService(@Qualifier("openai") ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
        You are an expert English language tutor.
        Analyze the user's original sentence and the translated (or intended) sentence.
        Provide a corrected version of the original sentence and specific feedback on grammar, vocabulary, or nuance errors.

        INPUT:
        - Original: The sentence written by the user (may contain errors).
        - Translated: The meaning the user intended to convey.

        OUTPUT FORMAT (JSON):
        {
          "correctedContent": "Corrected English sentence",
          "feedback": [
            {
              "tag": "GRAMMAR" | "VOCABULARY" | "EXPRESSION" | "NUANCE" | "SPELLING",
              "problem": "incorrect part",
              "correction": "corrected part",
              "extra": "Detailed explanation in Korean"
            }
          ]
        }

        RULES:
        - The 'extra' explanation MUST be in Korean.
        - If the original sentence is already perfect, 'correctedContent' should be the same as 'original', and 'feedback' should be an empty list.
        - Respond ONLY with the JSON object.
        """;

    public AiFeedbackResp analyze(AiFeedbackReq req) {
        String userMessage = String.format("Original: %s\nTranslated: %s", req.originalContent(), req.translatedContent());

        try {
            String response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                throw new RuntimeException("AI 응답이 비어있습니다.");
            }

            // JSON 파싱 (Markdown 코드 블록 제거 처리)
            String json = response.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(json, AiFeedbackResp.class);

        } catch (JsonProcessingException e) {
            log.error("AI 응답 JSON 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("AI 분석 결과를 처리하는 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("AI 분석 요청 실패: {}", e.getMessage());
            throw new RuntimeException("AI 분석 중 오류가 발생했습니다.", e);
        }
    }
}
