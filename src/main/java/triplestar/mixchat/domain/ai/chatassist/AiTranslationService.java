package triplestar.mixchat.domain.ai.chatassist;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.systemprompt.dto.TempAiReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.TempAiResp;

@Service
@RequiredArgsConstructor
public class AiTranslationService {

    private final ChatClient chatClient;

    public TempAiResp sendMessage(TempAiReq req) {
        // getPromptByKey(PromptKey.AI_TRANSLATION_PROMPT);
        // TODO : DB에 초기 프롬프트 데이터를 넣고 프롬프트 불러와 조합하는 로직으로 변경 필요

        String systemPrompt = """
                당신은 Mixchat의 영어 튜터입니다.
                사용자가 입력한 문장을 분석하여 번역과 태그, 교정, 설명을 포함하여 JSON 형식으로 응답합니다.:
                
                1) 한국어와 영어가 섞여있는 경우 자연스러운 영어 문장으로 다시 작성합니다.(TRANSLATION)
                2) 잘못된 문법(I goed → I went)도 교정합니다.(GRAMMAR)
                3) 문맥상 맞지만 부자연스러운 표현도 자연스럽게 바꿉니다.(VOCABULARY)
                부자연스러운 표현의 뜻과 더 자연스러운 표현의 뜻을 비교 설명합니다.
                4) 각 문제에 대해 태그, 문제 단어/구, 수정된 단어/구, 추가 설명을 포함한 피드백을 제공합니다.
                5) 출력 형식은 반드시 JSON입니다
                
                예시 입력: "나 감기 걸려서 기분이 blue I received a flu"
                
                출력 형식(JSON):
                original_content: 사용자가 입력한 원문
                corrected_content: 교정된 자연스러운 영어 문장
                feedback: [
                    {
                        tag: 문제 유형 (GRAMMAR, VOCABULARY, TRANSLATION 등)
                        problem: 문제 단어/구
                        correction: 수정된 단어/구
                        extra: 추가 설명
                    }, ... 
                ]    
                
                출력 예시:
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
                            "extra": "‘feeling blue’는 꽤 깊은 우울감을 나타내는 반면, 
                                ‘feel down’은 일시적으로 기분이 좋지 않거나 몸이 불편해서 처지는 상태를 나타낼 때 더 적절합니다."
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
                사용자 입력:
                {input}
                """;

        // TODO : 프롬프트 템플릿 엔진 도입 고려
        String prompt = systemPrompt.replace("{input}", req.message());
        String call = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

        // TODO : 응답 파싱 및 검증 로직 추가 필요
        // TODO : Strict JSON Mode 설정, Function Calling 활용 등

        return new TempAiResp(call);
    }
}
