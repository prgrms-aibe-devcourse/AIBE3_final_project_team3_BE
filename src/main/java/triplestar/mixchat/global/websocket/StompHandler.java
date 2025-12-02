package triplestar.mixchat.global.websocket;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.service.ChatMemberService;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.security.jwt.AccessTokenPayload;
import triplestar.mixchat.global.security.jwt.AuthJwtProvider;

@Slf4j
@Component
// stompHandler는 웹소켓 메시지를 가로채는 인터셉터
// order + 99를 통해 기본 인터셉터들보다는 늦게, 다른 커스텀보다는 빠르게 설정
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompHandler implements ExecutorChannelInterceptor {

    private final AuthJwtProvider authJwtProvider;
    private final MemberRepository memberRepository;
    private final ChatMemberService chatMemberService;

    private static final Pattern ROOM_DESTINATION_PATTERN =
            Pattern.compile("^/topic/(direct|group|ai)/rooms/(\\d+)");

    // 생성자를 직접 작성하고 @Lazy 어노테이션으로 순환 참조 해결
    public StompHandler(
            AuthJwtProvider authJwtProvider,
            MemberRepository memberRepository,
            @Lazy ChatMemberService chatMemberService) {
        this.authJwtProvider = authJwtProvider;
        this.memberRepository = memberRepository;
        this.chatMemberService = chatMemberService;
    }


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            throw new IllegalArgumentException("메시지 헤더를 찾을 수 없습니다.");
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message; // HEARTBEAT 등 커맨드 없는 경우
        }

        switch (command) {
            case CONNECT -> handleConnect(accessor);
            case SUBSCRIBE -> handleSubscribe(accessor);
            case SEND -> { /* preSend에서는 특별한 작업을 하지 않음. beforeHandle에서 처리. */ }
            case DISCONNECT -> { /* 연결 종료는 WebSocketEventListener에서 처리 */ }
            default -> { /* NO-OP */ }
        }
        return message;
    }

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        // Heart-beat 등 command가 없는 내부 메시지는 별도 처리 필요 없음
        if (accessor.getCommand() == null) {
            return message;
        }

        Principal principal = accessor.getUser();

        // beforeHandle에서 principal 객체 확인
        if (principal != null) {
            SecurityContextHolder.getContext().setAuthentication((Authentication) principal);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);

        if (token == null || token.isBlank()) {
            throw new AccessDeniedException("인증 토큰이 필요합니다.");
        }

        AccessTokenPayload payload = authJwtProvider.parseAccessToken(token);
        Long memberId = payload.memberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AccessDeniedException("사용자 정보를 찾을 수 없습니다. ID: " + memberId));

        CustomUserDetails userDetails = new CustomUserDetails(member.getId(), member.getRole(), member.getNickname());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        accessor.setUser(authentication);
        log.info("WebSocket 연결 성공 - memberId: {}, sessionId: {}", member.getId(), accessor.getSessionId());
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        Authentication authentication = requireAuth(accessor);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String destination = accessor.getDestination();
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("구독 목적지가 없습니다.");
        }

        // 채팅방 구독 시 멤버십 검증
        Matcher matcher = ROOM_DESTINATION_PATTERN.matcher(destination);
        if (matcher.matches()) {
            String typeString = matcher.group(1).toUpperCase();
            Long roomId = Long.parseLong(matcher.group(2));
            ChatRoomType chatRoomType =
                    ChatRoomType.valueOf(typeString);

            // 멤버십 검증 (캐시 사용으로 성능 최적화)
            chatMemberService.verifyUserIsMemberOfRoom(
                    userDetails.getId(), roomId, chatRoomType);
            return;
        }

        // 사용자별 목적지 구독 확인 (표준 방식)
        // /user/ 로 시작하는 모든 구독은 스프링이 현재 사용자의 세션에만 연결해주므로 안전함.
        if (destination.startsWith("/user/")) {
            return;
        }

        throw new AccessDeniedException("허용되지 않은 구독 목적지입니다: " + destination);
    }

    private Authentication requireAuth(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal instanceof Authentication && ((Authentication) principal).isAuthenticated()) {
            return (Authentication) principal;
        }
        throw new BadCredentialsException("인증된 사용자만 이 작업을 수행할 수 있습니다.");
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}