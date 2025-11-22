package triplestar.mixchat.domain.member.presence.controller;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import triplestar.mixchat.domain.member.presence.service.PresenceService;
import triplestar.mixchat.global.security.CustomUserDetails;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PresenceWebsocketController {

    private final PresenceService presenceService;

    @MessageMapping("/presence/heartbeat")
    public void heartbeat(Principal principal) {
        if (!(principal instanceof Authentication authentication)) {
            log.warn("인증 정보가 없이 webSocket heartbeat 요청이 들어왔습니다.");
            throw new BadCredentialsException("인증 정보가 없습니다.");
        }
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        Long id = customUserDetails.getId();
        log.debug("유저 heartbeat 정상 신호 : {}", id);

        presenceService.heartbeat(id);
    }
}
