package triplestar.mixchat.global.websocket;


import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
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
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;

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

            if (jwtToken != null && !jwtUtil.isExpired(jwtToken)) {
                String email = jwtUtil.getEmail(jwtToken);
                Member member = memberRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("인증된 사용자를 DB에서 찾을 수 없습니다."));

                CustomUserDetails userDetails = new CustomUserDetails(member);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                accessor.setUser(authentication);
            }
        } 
        else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            Principal principal = accessor.getUser();

                Authentication user = (Authentication) principal;
                CustomUserDetails userDetails = (CustomUserDetails) user.getPrincipal();
                
                Long id = userDetails.getId();
                Member member = memberRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다: " + id));
                Long memberId = member.getId();
            }
        }

        return message;
    }
}