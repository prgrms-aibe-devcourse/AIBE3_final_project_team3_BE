package triplestar.mixchat.domain.member.friend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.friend.dto.FriendshipRequestResp;
import triplestar.mixchat.domain.member.friend.repository.FriendshipRequestRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.testutils.TestMemberFactory;

@Transactional
@ActiveProfiles("test")
@DisplayName("친구 요청/처리 서비스")
@SpringBootTest
class FriendshipRequestServiceTest {

    @Autowired
    private FriendshipRequestService friendshipRequestService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private FriendshipRequestRepository friendshipRequestRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private Member member2;
    private Member member3;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(TestMemberFactory.createMember("user1"));
        member2 = memberRepository.save(TestMemberFactory.createMember("user2"));
        member3 = memberRepository.save(TestMemberFactory.createMember("user3"));
    }

    @Test
    @DisplayName("친구 요청 성공 생성 테스트")
    void send_success() {
        friendshipRequestService.sendRequest(member1.getId(), member2.getId());
        friendshipRequestService.sendRequest(member1.getId(), member3.getId());
        friendshipRequestService.sendRequest(member2.getId(), member3.getId());
    }

    @Test
    @DisplayName("친구 요청 실패 자기 자신 테스트")
    void send_fail_self() {
        assertThatThrownBy(() -> friendshipRequestService.sendRequest(member1.getId(), member1.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("자기 자신에게 친구 요청을 보낼 수 없습니다.");
    }

    @Test
    @DisplayName("친구 요청 실패 중복 요청")
    void send_fail_duplicate() {
        friendshipRequestService.sendRequest(member1.getId(), member2.getId());

        assertThatThrownBy(() -> friendshipRequestService.sendRequest(member1.getId(), member2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 친구 요청이 존재합니다.");

        assertThatThrownBy(() -> friendshipRequestService.sendRequest(member2.getId(), member1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 친구 요청이 존재합니다.");
    }

    @Test
    @DisplayName("친구 요청 수락/거절 성공 - 수락")
    void process_success_accept() {
        FriendshipRequestResp friendshipRequestResp = friendshipRequestService
                .sendRequest(member1.getId(), member2.getId());
        Long requestId = friendshipRequestResp.id();
        friendshipRequestService.processRequest(member2.getId(), requestId, true);

        assertThat(friendshipService.isFriends(member1.getId(), member2.getId())).isTrue();
        assertThat(friendshipService.isFriends(member2.getId(), member1.getId())).isTrue();

        // 처리 후 요청 삭제 확인
        assertThat(friendshipRequestRepository.findById(requestId).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("친구 요청 수락/거절 이미 친구관계")
    void send_fail_already_friends() {
        FriendshipRequestResp friendshipRequestResp = friendshipRequestService
                .sendRequest(member1.getId(), member2.getId());
        Long requestId = friendshipRequestResp.id();
        friendshipRequestService.processRequest(member2.getId(), requestId, true);

        assertThatThrownBy(() -> friendshipRequestService.sendRequest(member1.getId(), member2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 친구 관계입니다.");

        assertThatThrownBy(() -> friendshipRequestService.sendRequest(member2.getId(), member1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 친구 관계입니다.");
    }

    @Test
    @DisplayName("친구 요청 수락/거절 성공 - 거절")
    void process_success_reject() {
        FriendshipRequestResp friendshipRequestResp = friendshipRequestService
                .sendRequest(member1.getId(), member2.getId());
        Long requestId = friendshipRequestResp.id();
        friendshipRequestService.processRequest(member2.getId(), requestId, false);

        assertThat(friendshipService.isFriends(member1.getId(), member2.getId())).isFalse();
        assertThat(friendshipService.isFriends(member2.getId(), member1.getId())).isFalse();

        // 처리 후 요청 삭제 확인
        assertThat(friendshipRequestRepository.findById(requestId).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("친구 요청 수락/거절 실패 - 존재하지 않는 요청")
    void process_fail_not_found() {
        friendshipRequestService.sendRequest(member1.getId(), member2.getId());

        assertThatThrownBy(() -> friendshipRequestService.processRequest(member2.getId(), 0L, false))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("존재하지 않는 친구 요청입니다.");
    }

    @Test
    @DisplayName("친구 요청 수락/거절 실패 - 요청 받은사람만 처리 가능")
    void process_fail_access_denied() {
        FriendshipRequestResp friendshipRequestResp = friendshipRequestService
                .sendRequest(member1.getId(), member2.getId());
        Long requestId = friendshipRequestResp.id();

        assertThatThrownBy(() -> friendshipRequestService.processRequest(member1.getId(), requestId, false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("본인이 받은 친구 요청만 처리할 수 있습니다.");
    }

    @Test
    @DisplayName("친구 관계 삭제 성공")
    void delete_friendship_success() {
        FriendshipRequestResp friendshipRequestResp = friendshipRequestService
                .sendRequest(member1.getId(), member2.getId());
        Long requestId = friendshipRequestResp.id();
        friendshipRequestService.processRequest(member2.getId(), requestId, true);

        assertThat(friendshipService.isFriends(member1.getId(), member2.getId())).isTrue();
        assertThat(friendshipService.isFriends(member2.getId(), member1.getId())).isTrue();

        friendshipService.deleteFriendship(member1.getId(), member2.getId());

        assertThat(friendshipService.isFriends(member1.getId(), member2.getId())).isFalse();
        assertThat(friendshipService.isFriends(member2.getId(), member1.getId())).isFalse();
    }
}