package triplestar.mixchat.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.domain.notification.dto.NotificationReq;
import triplestar.mixchat.domain.notification.dto.NotificationResp;
import triplestar.mixchat.domain.notification.entity.Notification;
import triplestar.mixchat.domain.notification.repository.NotificationRepository;
import triplestar.mixchat.testutils.TestMemberFactory;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    MemberRepository memberRepository;

    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(TestMemberFactory.createMember("user1"));
        member2 = memberRepository.save(TestMemberFactory.createMember("user2"));
    }

    @Test
    @DisplayName("알림 생성 테스트")
    void createNotificationTest() {
        NotificationReq req = new NotificationReq(
                member2.getId(),
                NotificationType.FRIEND_REQUEST,
                NotificationType.FRIEND_REQUEST.formatContent(member1.getName())
        );

        NotificationResp resp = notificationService.createNotification(req);

        assertThat(resp.id()).isNotNull();
        assertThat(resp.type()).isEqualTo(NotificationType.FRIEND_REQUEST);
        assertThat(resp.content()).isEqualTo("user1님이 친구 요청을 보냈습니다.");
        assertThat(resp.receiverId()).isEqualTo(member2.getId());
        assertThat(resp.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("알림 생성 시 잘못된 회원 ID로 예외 발생 테스트")
    void createNotificationInvalidMemberTest() {
        NotificationReq req = new NotificationReq(
                Long.MAX_VALUE,
                NotificationType.FRIEND_REQUEST,
                NotificationType.FRIEND_REQUEST.formatContent(member1.getName())
        );

        Assertions.assertThatThrownBy(() -> {
            notificationService.createNotification(req);
        }).isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("해당 회원 ID 없음");
    }
}