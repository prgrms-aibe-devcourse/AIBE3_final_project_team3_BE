package triplestar.mixchat.testutils;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import triplestar.mixchat.global.ai.BotMemberIdProvider;

@Component
@Profile("test")
public class TestBotMemberProvider implements BotMemberIdProvider {

    @Override
    public Long getBotMemberId() {
        return 1000L;
    }
}