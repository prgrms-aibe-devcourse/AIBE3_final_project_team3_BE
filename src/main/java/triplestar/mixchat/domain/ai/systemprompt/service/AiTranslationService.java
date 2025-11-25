package triplestar.mixchat.domain.ai.systemprompt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiTranslationReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiTranslationResp;

@Service
@RequiredArgsConstructor
public class AiTranslationService {

    private final OpenAiChatModel chatModel;

    public AiTranslationResp sendMessage(AiTranslationReq req) {
        // 임시 프롬프트
        String systemPrompt = """
                당신은 Mixchat의 영어 튜터입니다.
                사용자가 입력한 문장을 분석하여 번역과 태그, 교정, 설명을 포함하여 JSON 형식으로 응답합니다.:
                
                1) 한국어와 영어가 섞여있는 경우 자연스러운 영어 문장으로 다시 작성합니다.(TRANSLATION)
                2) 잘못된 문법(I goed → I went)도 교정합니다.(GRAMMAR)
                3) 문맥상 맞지만 부자연스러운 표현도 자연스럽게 바꿉니다.(VOCABULARY)
                4) 각 문제에 대해 태그, 문제 단어/구, 수정된 단어/구, 추가 설명을 포함한 피드백을 제공합니다.
                5) 출력 형식은 반드시 JSON입니다
                
                예시 입력: "나 감기 걸려서 기분이 blue I received a flu"
                예시 출력(JSON):
                {
                    "original_content": "나 감기 걸려서 기분이 blue I received flu",
                    "corrected_content": "I feel a bit down because I caught the flu.",
                    "feedback": [
                        {
                            "tag": "VOCABULARY",
                            "problem": "received a flu",
                            "correction": "caught the flu",
                            "extra": "'감기에 걸리다'는 'catch the flu' 혹은 'get the flu'가 더 자연스러운 관용 표현입니다."
                        },
                        {
                            "tag": "VOCABULARY",
                            "problem": "feeling blue",
                            "correction": "feel (a bit) down",
                            "extra": "문맥상 '감기 때문에 기분이 안 좋다'는 뜻이므로 'feel down'이 'blue'보다 더 적절합니다."
                        },
                        {
                            "tag": "TRANSLATION",
                            "problem": "감기 걸려서",
                            "correction": "caught the flu",
                            "extra": "한국어 접속 표현을 자연스러운 영어로 연결합니다."
                        },
                        {
                            "tag": "GRAMMAR",
                            "problem": "flu",
                            "correction": "the(혹은 a) flu",
                            "extra": "'flu'와 같은 셀 수 있는 명사나 질병 이름 앞에는 'a' 또는 'the'와 같은 관사를 붙여야 합니다. (예: I have the flu)."
                        }
                    ]
                }
                이 규칙을 항상 지키세요.
                {input}
                """;

        // TODO : 프롬프트 템플릿 엔진 도입 고려
        String prompt = systemPrompt.replace("{input}", req.message());
        String call = chatModel.call(prompt);

        // TODO : 응답 파싱 및 검증 로직 추가 필요
        // TODO : Strict JSON Mode 설정, Function Calling 활용 등

        return new AiTranslationResp(call);
    }
}
