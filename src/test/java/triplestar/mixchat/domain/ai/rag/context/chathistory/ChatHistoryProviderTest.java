package triplestar.mixchat.domain.ai.rag.context.chathistory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.testutils.TestMemberFactory;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
@DisplayName("채팅 히스토리 제공자 테스트")
class ChatHistoryProviderTest {

    @Autowired ChatMessageService chatMessageService;
    @Autowired MemberRepository memberRepository;
    Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(TestMemberFactory.createMember("user1"));
    }

    @Test
    void getRecentHistory() {
    }
}