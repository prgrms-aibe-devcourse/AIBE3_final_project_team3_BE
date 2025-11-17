package triplestar.mixchat.global.websocket;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.security.jwt.AccessTokenPayload;
import triplestar.mixchat.global.security.jwt.AuthJwtProvider;

import java.security.Principal;

@Slf4j
@Component
// @RequiredArgsConstructor 제거
// stompHandler는 웹소켓 메시지를 가로채는 인터셉터
// order + 99를 통해 기본 인터셉터들보다는 늦게, 다른 커스텀보다는 빠르게 설정
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompHandler implements ChannelInterceptor {

    private final AuthJwtProvider authJwtProvider;
    private final MemberRepository memberRepository;
    private final ChatRoomService chatRoomService;

    // 생성자를 직접 작성하고 @Lazy 어노테이션으로 순환 참조 해결
    public StompHandler(AuthJwtProvider authJwtProvider, MemberRepository memberRepository, @Lazy ChatRoomService chatRoomService) {
        this.authJwtProvider = authJwtProvider;
        this.memberRepository = memberRepository;
        this.chatRoomService = chatRoomService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            throw new IllegalArgumentException("메시지 헤더를 찾을 수 없습니다.");
        }

        StompCommand command = accessor.getCommand();
        if (command == null) return message; // HEARTBEAT 등 커맨드 없는 경우

        switch (command) {
            case CONNECT -> handleConnect(accessor);
            case SUBSCRIBE -> handleSubscribe(accessor);
            case DISCONNECT -> log.info("STOMP DISCONNECT: sessionId={}", accessor.getSessionId());
            default -> { /* NO-OP */ }
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);

        if (token == null || token.isBlank()) {
            throw new AccessDeniedException("인증 토큰이 필요합니다.");
        }

        try {
            AccessTokenPayload payload = authJwtProvider.parseAccessToken(token);
            Long memberId = payload.memberId();

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new AccessDeniedException("사용자 정보를 찾을 수 없습니다. ID: " + memberId));

            CustomUserDetails userDetails = new CustomUserDetails(member.getId(), member.getRole());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            accessor.setUser(authentication);

            log.info("STOMP CONNECT (Authenticated): memberId={}, sessionId={}", memberId, accessor.getSessionId());
        } catch (ExpiredJwtException e) {
            throw new AccessDeniedException("만료된 토큰입니다.", e);
        } catch (SignatureException | MalformedJwtException e) {
            throw new AccessDeniedException("유효하지 않은 형식의 토큰입니다.", e);
        } catch (Exception e) {
            log.error("STOMP CONNECT: 예상치 못한 인증 오류 발생", e);
            throw new AccessDeniedException("인증 처리 중 오류가 발생했습니다.", e);
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        Authentication authentication = requireAuth(accessor);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String destination = accessor.getDestination();
        if (destination == null || destination.isBlank()) {
            throw new AccessDeniedException("구독 목적지가 없습니다.");
        }

        // 채팅방 구독 권한 확인
        Long roomId = chatRoomService.getRoomIdFromDestination(destination);
        if (roomId != null) {
            chatRoomService.verifyUserIsMemberOfRoom(userDetails.getId(), roomId);
            log.info("SUBSCRIBE (Room): memberId={} destination={}", userDetails.getId(), destination);
            return;
        }

        // 사용자별 토픽 구독 권한 확인 (ex. 채팅방 목록 업데이트)
        String userTopicPrefix = "/topic/user/" + userDetails.getId();
        if (destination.startsWith(userTopicPrefix)) {
            log.info("SUBSCRIBE (User Topic): memberId={} destination={}", userDetails.getId(), destination);
            return;
        }

        throw new AccessDeniedException("허용되지 않은 구독 목적지입니다: " + destination);
    }

    private Authentication requireAuth(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal instanceof Authentication auth && auth.isAuthenticated()) {
            return auth;
        }
        throw new AccessDeniedException("인증된 사용자만 이 작업을 수행할 수 있습니다.");
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}
