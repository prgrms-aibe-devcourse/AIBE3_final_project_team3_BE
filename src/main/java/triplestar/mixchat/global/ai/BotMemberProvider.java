package triplestar.mixchat.global.ai;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.member.member.constant.Role;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

@Getter
@Component
@RequiredArgsConstructor
public class BotMemberProvider {

    private final MemberRepository memberRepository;

    private Long botMemberId;

    @PostConstruct
    public void init() {
        this.botMemberId = memberRepository.findByRole(Role.ROLE_BOT)
                .orElseThrow(() ->
                        new IllegalStateException("BOT 멤버가 존재하지 않습니다."))
                .getId();
    }
}
