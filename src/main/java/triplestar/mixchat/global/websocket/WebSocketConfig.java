package triplestar.mixchat.global.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${spring.rabbitmq.stomp.relay-host}")
    private String relayHost;

    @Value("${spring.rabbitmq.stomp.relay-port}")
    private int relayPort;

    @Value("${spring.rabbitmq.stomp.client-login}")
    private String clientLogin;

    @Value("${spring.rabbitmq.stomp.client-passcode}")
    private String clientPasscode;

    @Value("${spring.rabbitmq.stomp.system-login}")
    private String systemLogin;

    @Value("${spring.rabbitmq.stomp.system-passcode}")
    private String systemPasscode;

    @Value("${spring.rabbitmq.stomp.system-heartbeat-send-interval}")
    private long systemHeartbeatSendInterval;

    @Value("${spring.rabbitmq.stomp.system-heartbeat-receive-interval}")
    private long systemHeartbeatReceiveInterval;

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
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(relayHost)    // RabbitMQ 호스트 주소
                .setRelayPort(relayPort)    // STOMP 포트
                .setClientLogin(clientLogin)    // 클라이언트 로그인
                .setClientPasscode(clientPasscode)
                .setSystemLogin(systemLogin)    // 시스템 관리자 로그인
                .setSystemPasscode(systemPasscode)
                .setSystemHeartbeatSendInterval(systemHeartbeatSendInterval)    //일정 시간마다 heartbeat 전송
                .setSystemHeartbeatReceiveInterval(systemHeartbeatReceiveInterval); // 일정 시간마다 heartbeat 수신 확인

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
}