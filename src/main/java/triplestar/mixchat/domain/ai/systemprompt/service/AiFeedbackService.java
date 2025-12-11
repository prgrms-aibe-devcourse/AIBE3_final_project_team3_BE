package triplestar.mixchat.domain.ai.systemprompt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackResp;

@Slf4j
@Service
public class AiFeedbackService {

    private final ChatClient chatClient;
    private final BeanOutputConverter<AiFeedbackResp> converter;

    public AiFeedbackService(@Qualifier("openai") ChatClient chatClient) {
        this.chatClient = chatClient;
        this.converter = new BeanOutputConverter<>(AiFeedbackResp.class);
    }

    private static final String SYSTEM_PROMPT_TEMPLATE = """
        You are an expert English language tutor.
        Your task is to correct the user's English sentence to be more natural and grammatically correct, considering the intended meaning (Context).

        INPUT:
        - Original: The sentence written by the user (may contain errors).
        - Context: The intended meaning (usually in the user's native language, e.g., Korean) or a translation.

        INSTRUCTIONS:
        1. Analyze the 'Original' sentence against the 'Context'.
        2. Generate a 'correctedContent' version of the sentence that is natural and accurate English.
        3. Provide a list of specific feedback items explaining the errors or improvements.
        4. For the 'tag' field in feedback, use one of: GRAMMAR, VOCABULARY, TRANSLATION, EXPRESSION.
        5. The 'extra' explanation MUST be written in KOREAN.

        {format}
        """;

    public AiFeedbackResp analyze(AiFeedbackReq req) {
        String userMessage = String.format("Original: %s\nContext: %s", req.originalContent(), req.translatedContent());

        try {
            return chatClient.prompt()
                    .system(sp -> sp.text(SYSTEM_PROMPT_TEMPLATE)
                            .param("format", converter.getFormat())) // JSON 포맷 강제
                    .user(userMessage)
                    .call()
                    .entity(AiFeedbackResp.class); // 자동 변환

        } catch (Exception e) {
            log.error("AI 피드백 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new IllegalStateException("AI 분석에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }
}

