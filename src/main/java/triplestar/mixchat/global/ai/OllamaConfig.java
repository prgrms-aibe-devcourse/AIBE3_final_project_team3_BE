package triplestar.mixchat.global.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

// Ollama WebUI 설정 - openAI 호환 API 사용
@Configuration
public class OllamaConfig {

    @Value("${spring.ai.ollama.base-url}")
    private String baseUrl;

    @Value("${spring.ai.ollama.api-key}")
    private String apiKey;

    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;

    @Bean
    public OpenAiApi ollamaOpenAiApi() {
        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
    }

    @Bean
    public OpenAiChatModel ollamaOpenAiChatModel(@Qualifier("ollamaOpenAiApi") OpenAiApi ollamaOpenAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(ollamaOpenAiApi)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model(model)
                                .build()
                )
                .build();
    }

    @Bean
    @Order(1)
    @Qualifier("ollama")
    public ChatClient ollamaChatClient(@Qualifier("ollamaOpenAiChatModel") OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }
}
