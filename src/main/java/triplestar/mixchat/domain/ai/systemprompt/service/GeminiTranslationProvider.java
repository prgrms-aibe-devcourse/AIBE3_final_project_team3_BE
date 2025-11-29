package triplestar.mixchat.domain.ai.systemprompt.service;

//@Slf4j
//// @Component
//@RequiredArgsConstructor
//public class GeminiTranslationProvider implements TranslationProvider {
//
//    // TODO: build.gradle 또는 pom.xml에 'spring-ai-google-gemini-starter' 의존성 추가가 필요합니다.
////    private final GoogleGeminiChatModel geminiChatModel;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public Mono<TranslationResp> translate(String originalContent) {
//        String systemPrompt = """
//            You are Mixchat's English tutor. Analyze the user's sentence and respond in JSON format with translation, tags, corrections, and explanations.
//
//            RULES:
//            1. If the user's sentence is already a natural and grammatically correct English sentence, respond with a JSON where "corrected_content" is null.
//            2. If the sentence is in Korean, a mix of languages, or is unnatural/incorrect English, translate and correct it into a single, natural English sentence.
//            3. Provide feedback for each issue found, including tag, problem, correction, and extra explanation.
//            4. The output format MUST be a single raw JSON object. Do not include any other text or markdown.
//
//            JSON Structure:
//            {
//              "original_content": (The user's original input string),
//              "corrected_content": (The corrected, natural English sentence. Null if no changes are needed.),
//              "feedback": [
//                {
//                  "tag": (Issue type: GRAMMAR, VOCABULARY, TRANSLATION),
//                  "problem": (The problematic word/phrase),
//                  "correction": (The corrected word/phrase),
//                  "extra": (Explanation of the correction)
//                }, ...
//              ]
//            }
//
//            User input:
//            {input}
//            """;
//
//        String finalPrompt = systemPrompt.replace("{input}", originalContent);
//
//        return Mono.fromCallable(() -> {
//            // Gemini는 JSON 응답을 보장하기 위해 프롬프트에 추가적인 지시가 필요할 수 있습니다.
//            ChatResponse response = geminiChatModel.call(new Prompt(finalPrompt));
//            String jsonResponse = response.getResult().getOutput().getContent();
//
//            // Gemini가 JSON 마크다운(` ```json ... ``` `)으로 감싸서 반환하는 경우가 많으므로, 이를 제거합니다.
//            jsonResponse = jsonResponse.replace("```json", "").replace("```", "").trim();
//
//            try {
//                return objectMapper.readValue(jsonResponse, TranslationResp.class);
//            } catch (IOException e) {
//                log.error("Gemini 응답 JSON 파싱 실패: {}", jsonResponse, e);
//                throw new RuntimeException("Failed to parse Gemini response", e);
//            }
//        });
//    }
//
//    @Override
//    public int getOrder() {
//        return 3; // 3순위
//    }
//}
