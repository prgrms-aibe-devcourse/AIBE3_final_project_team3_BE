package triplestar.mixchat.global.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient.Builder chatClientBuilder(OllamaChatModel model) {
        return ChatClient.builder(model);
    }

    // Ollama Model
    @Bean
    @Primary
    @Qualifier("ollama")
    public ChatClient ollamaChatClient(OllamaChatModel model) {
        return ChatClient.builder(model).build();
    }

    // OpenAI Model
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
