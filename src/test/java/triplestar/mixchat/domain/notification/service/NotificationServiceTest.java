package triplestar.mixchat.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    NotificationService notificationService;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(TestMemberFactory.createMember("user1"));
        member2 = memberRepository.save(TestMemberFactory.createMember("user2"));
    }

    @Test
    @DisplayName("알림 생성 성공")
    void create_notification_success() {
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
    @DisplayName("알림 생성 실패 - 존재하지 않는 회원")
    void create_notification_fail() {
        NotificationReq req = new NotificationReq(
                Long.MAX_VALUE,
                NotificationType.FRIEND_REQUEST,
                NotificationType.FRIEND_REQUEST.formatContent(member1.getName())
        );

        Assertions.assertThatThrownBy(() -> notificationService.createNotification(req))
                .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("해당 회원 ID 없음");
    }

    @Test
    @DisplayName("알림 조회 성공")
    void get_notifications_success() {
        for (int i = 0; i < 5; i++) {
            NotificationReq req = new NotificationReq(
                    member2.getId(),
                    NotificationType.MESSAGE,
                    NotificationType.MESSAGE.formatContent(member1.getName())
            );
            notificationService.createNotification(req);
        }

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationResp> notificationsPage = notificationService.getNotifications(member2.getId(), pageRequest);

        assertThat(notificationsPage.getTotalElements()).isEqualTo(5);
        assertThat(notificationsPage.getContent()).allSatisfy(notificationResp -> {
            assertThat(notificationResp.receiverId()).isEqualTo(member2.getId());
            assertThat(notificationResp.type()).isEqualTo(NotificationType.MESSAGE);
            assertThat(notificationResp.content()).isEqualTo("user1님이 메시지를 보냈습니다.");
        });
    }

    @Test
    @DisplayName("알람 조회 성공 - 알림 없음")
    void get_notifications_empty() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationResp> notificationsPage = notificationService.getNotifications(member2.getId(), pageRequest);

        assertThat(notificationsPage.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void mark_as_read_success() {
        NotificationReq req = new NotificationReq(
                member2.getId(),
                NotificationType.MESSAGE,
                NotificationType.MESSAGE.formatContent(member1.getName())
        );
        NotificationResp resp = notificationService.createNotification(req);

        // 읽음 처리 전 상태 확인
        Notification before = notificationRepository.findById(resp.id())
                .orElseThrow(AssertionError::new);
        assertThat(before.isRead()).isFalse();

        notificationService.markAsRead(member2.getId(), resp.id());

        // 읽음 처리 후 상태 확인
        Notification after = notificationRepository.findById(resp.id())
                .orElseThrow(AssertionError::new);
        assertThat(after.isRead()).isTrue();
    }

    @Test
    @DisplayName("알람 읽음 처리 실패 - 존재하지 않는 알림")
    void mark_as_read_not_found() {
        Assertions.assertThatThrownBy(() -> notificationService.markAsRead(member2.getId(), Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 성공")
    void mark_all_as_read_success() {
        for (int i = 0; i < 3; i++) {
            NotificationReq req = new NotificationReq(
                    member2.getId(),
                    NotificationType.MESSAGE,
                    NotificationType.MESSAGE.formatContent(member1.getName())
            );
            notificationService.createNotification(req);
        }

        notificationService.markAllAsRead(member2.getId());

        // 벌크 연산 후 영속성 컨텍스트 초기화
        em.flush();
        em.clear();

        notificationRepository.findAllByReceiverId(member2.getId(), PageRequest.of(0, 10))
                .forEach(notification -> assertThat(notification.isRead()).isTrue());
    }

    @Test
    @DisplayName("알림 삭제 성공")
    void delete_notification_success() {
        NotificationReq req = new NotificationReq(
                member2.getId(),
                NotificationType.CHAT_INVITATION,
                NotificationType.CHAT_INVITATION.formatContent(member1.getName())
        );
        NotificationResp resp = notificationService.createNotification(req);

        // 삭제 전 존재 여부 확인
        notificationRepository.findById(resp.id())
                .orElseThrow(AssertionError::new);

        notificationService.deleteNotification(member2.getId(), resp.id());

        // 삭제 후 존재 여부 확인
        boolean empty = notificationRepository.findById(resp.id()).isEmpty();
        assertThat(empty).isTrue();
    }

    @Test
    @DisplayName("알람 삭제 실패 - 존재하지 않는 알림")
    void delete_notification_not_found() {
        Assertions.assertThatThrownBy(() -> notificationService.deleteNotification(member2.getId(), Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("모든 알림 삭제 성공")
    void delete_all_notifications_success() {
        for (int i = 0; i < 4; i++) {
            NotificationReq req = new NotificationReq(
                    member2.getId(),
                    NotificationType.SYSTEM_ALERT,
                    NotificationType.SYSTEM_ALERT.formatContent("")
            );
            notificationService.createNotification(req);
        }

        notificationService.deleteAllNotifications(member2.getId());

        Page<NotificationResp> notificationsPage = notificationService.getNotifications(
                member2.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(notificationsPage.getTotalElements()).isEqualTo(0);
    }
}