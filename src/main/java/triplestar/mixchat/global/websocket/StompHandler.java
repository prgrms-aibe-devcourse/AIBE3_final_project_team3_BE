package triplestar.mixchat.global.websocket;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.security.jwt.AccessTokenPayload;
import triplestar.mixchat.global.security.jwt.AuthJwtProvider;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
// stompHandler는 웹소켓 메시지를 가로채는 인터셉터
// order + 99를 통해 기본 인터셉터들보다는 늦게, 다른 커스텀보다는 빠르게 설정
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompHandler implements ChannelInterceptor {

    private final AuthJwtProvider authJwtProvider;
    private final MemberRepository memberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            throw new SecurityException("메시지 헤더를 찾을 수 없습니다.");
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String jwtToken = accessor.getFirstNativeHeader("Authorization");

            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                jwtToken = jwtToken.substring(7);
            }

            if (jwtToken != null) {
                try {
                    AccessTokenPayload payload = authJwtProvider.parseAccessToken(jwtToken);
                    Long memberId = payload.memberId();
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new RuntimeException("인증된 사용자를 DB에서 찾을 수 없습니다."));

                    CustomUserDetails userDetails = new CustomUserDetails(member.getId(), member.getRole());
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    accessor.setUser(authentication);

                    log.info("STOMP CONNECT: 사용자 인증 성공. 회원 ID: {}", memberId);

                } catch (Exception e) {
                    log.error("STOMP CONNECT: 토큰이 유효하지 않거나 사용자를 찾을 수 없습니다. 오류: {}", e.getMessage(), e);
                    throw new SecurityException("유효하지 않은 토큰입니다.", e);
                }
            } else {
                log.warn("STOMP CONNECT: Authorization 헤더에서 JWT 토큰을 찾을 수 없습니다.");
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            if (principal != null) {
                Authentication user = (Authentication) principal;
                CustomUserDetails userDetails = (CustomUserDetails) user.getPrincipal();

                Long id = userDetails.getId();
                memberRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다: " + id));
            }
        }

        return message;
    }
}
