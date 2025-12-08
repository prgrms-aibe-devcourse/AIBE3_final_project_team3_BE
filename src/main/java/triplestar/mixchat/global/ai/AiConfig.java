package triplestar.mixchat.global.ai;

import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.ollama.api-key}")
    private String ollamaApiKey;

    @Value("${spring.ai.ollama.chat.options.model}")
    private String ollamaModel;

    // OpenAi는 Auto Configuration 사용으로 별도 설정 불필요

    // OLLAMA API 설정
    // OLLAMA 또한 openWebUI를 기반으로 OpenAI API를 흉내내므로 OpenAiApi 사용
    @Bean
    public OpenAiApi ollamaOpenAiApi() {
        return OpenAiApi.builder()
                .baseUrl(ollamaBaseUrl)
                .apiKey(ollamaApiKey)
                .build();
    }

    @Bean
    @Primary
    public OpenAiChatModel ollamaOpenAiChatModel(@Qualifier("ollamaOpenAiApi") OpenAiApi ollamaOpenAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(ollamaOpenAiApi)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model(ollamaModel)
                                .build()
                )
                .build();
    }

    @Bean
    @Primary
    @Qualifier("ollama")
    public ChatClient ollamaChatClient(@Qualifier("ollamaOpenAiChatModel") OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }

    @Bean
    @Qualifier("openai")
    public ChatClient openAiChatClient(OpenAiChatModel model) {
        return ChatClient.builder(model).build();
    }

    // Gemini Model (Vertex AI 설정 필요 - 당장 사용하지 않으므로 비활성화)
//    @Bean
//    @Qualifier("gemini")
//    public ChatClient geminiChatClient(VertexAiGeminiChatModel model) {
//        return ChatClient.builder(model).build();
//    }
}
