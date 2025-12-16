package triplestar.mixchat.global.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@EnableConfigurationProperties(WebSocketConfig.StompProperties.class)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    private final StompProperties stompProperties;
    private final StompHandler stompHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 웹소켓 연결을 시작할 엔드포인트를 설정
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(allowedOrigins); // 환경변수에서 설정된 출처만 허용
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // RabbitMQ STOMP Relay 설정 (수평 확장 지원)
        registry.enableStompBrokerRelay("/topic", "/queue", "/exchange")
                .setRelayHost(stompProperties.relayHost())    // RabbitMQ 호스트 주소
                .setRelayPort(stompProperties.relayPort())    // STOMP 포트
                .setClientLogin(stompProperties.clientLogin())    // 클라이언트 로그인
                .setClientPasscode(stompProperties.clientPasscode())
                .setSystemLogin(stompProperties.systemLogin())    // 시스템 관리자 로그인
                .setSystemPasscode(stompProperties.systemPasscode())
                .setSystemHeartbeatSendInterval(stompProperties.systemHeartbeatSendInterval())    //일정 시간마다 heartbeat 전송
                .setSystemHeartbeatReceiveInterval(stompProperties.systemHeartbeatReceiveInterval()); // 일정 시간마다 heartbeat 수신 확인

        // 클라이언트에서 서버로 메시지를 보낼 때 사용하는 프리픽스를 설정
        // 예를 들어, 클라이언트는 /app/chat.sendMessage 와 같은 경로로 메시지를 전송
        registry.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게 메시지를 보낼 때 사용하는 프리픽스를 설정합니다.
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // StompHandler(인증/인가 처리)
        registration.interceptors(stompHandler);
    }

    @ConfigurationProperties(prefix = "spring.rabbitmq.stomp")
    public record StompProperties(
            String relayHost,
            int relayPort,
            String clientLogin,
            String clientPasscode,
            String systemLogin,
            String systemPasscode,
            long systemHeartbeatSendInterval,
            long systemHeartbeatReceiveInterval
    ) {}
}
