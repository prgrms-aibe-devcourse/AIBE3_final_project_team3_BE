package triplestar.mixchat.domain.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.domain.notification.entity.Notification;
import triplestar.mixchat.domain.notification.repository.NotificationRepository;
import triplestar.mixchat.testutils.TestMemberFactory;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("알람 컨트롤러")
class ApiV1NotificationControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private Member member2;
    private Notification notification1;
    private Notification notification2;
    private Notification notification3;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(TestMemberFactory.createMember("user1"));
        member2 = memberRepository.save(TestMemberFactory.createMember("user2"));

        notification1 = notificationRepository.save(Notification.builder()
                        .receiver(member1)
                        .type(NotificationType.FRIEND_REQUEST).build()
        );
        notification2 = notificationRepository.save(Notification.builder()
                .receiver(member1)
                .type(NotificationType.CHAT_INVITATION).build()
        );
        // member2에게 보내진 알림 (member1로 로그인했을 때 조회되지 않아야 함)
        notification3 = notificationRepository.save(Notification.builder()
                .receiver(member2)
                .type(NotificationType.MESSAGE).build()
        );
    }

    @Test
    @DisplayName("알람목록 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void notifications_success() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/notifications")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1NotificationController.class))
                .andExpect(handler().methodName("getNotifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(2))

                // 최신순 DESC 정렬 검증 (notification2가 먼저)
                .andExpect(jsonPath("$.data.content[0].type").value(notification2.getType().name()))
                .andExpect(jsonPath("$.data.content[0].receiverId").value(member1.getId()))
                .andExpect(jsonPath("$.data.content[1].type").value(notification1.getType().name()))
                .andExpect(jsonPath("$.data.content[1].receiverId").value(member1.getId()));
    }

    @Test
    @DisplayName("알람 목록 실패 - 인증없음")
    void notifications_fail() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/notifications")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("모든 알람 읽음 처리 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mark_all_as_read_success() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        patch("/api/v1/notifications/read-all")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1NotificationController.class))
                .andExpect(handler().methodName("markAllAsRead"))
                .andExpect(status().isOk());

        // DB 검증 생략 영속성 컨텍스트 초기화 필요
    }

    @Test
    @DisplayName("알람 읽음 처리 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mark_as_read_success() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        patch("/api/v1/notifications/read/{id}", notification1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(ApiV1NotificationController.class))
                .andExpect(handler().methodName("markAsRead"))
                .andExpect(status().isOk());

        // DB 검증
        Notification updatedNotification = notificationRepository.findById(notification1.getId()).get();
        assertThat(updatedNotification.isRead()).isTrue();

        Notification notRead = notificationRepository.findById(notification2.getId()).get();
        assertThat(notRead.isRead()).isFalse();
    }

    @Test
    @DisplayName("알람 읽음 처리 실패 - 본인 알람 아님")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mark_as_read_fail_not_my_notification() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        patch("/api/v1/notifications/read/{id}", notification3.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1NotificationController.class))
                .andExpect(handler().methodName("markAsRead"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("모든 알람 삭제 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void delete_all_notifications_success() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/notifications")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1NotificationController.class))
                .andExpect(handler().methodName("deleteAllNotification"))
                .andExpect(status().isOk());

        // DB 검증
        boolean exists1 = notificationRepository.existsById(notification1.getId());
        boolean exists2 = notificationRepository.existsById(notification2.getId());
        assertThat(exists1).isFalse();
        assertThat(exists2).isFalse();
    }

    @Test
    @DisplayName("알람 삭제 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void delete_notification_success() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/notifications/{id}", notification1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(ApiV1NotificationController.class))
                .andExpect(handler().methodName("deleteNotification"))
                .andExpect(status().isOk());

        // DB 검증
        boolean exists = notificationRepository.existsById(notification1.getId());
        assertThat(exists).isFalse();

        boolean stillExists = notificationRepository.existsById(notification2.getId());
        assertThat(stillExists).isTrue();
    }

    @Test
    @DisplayName("알람 삭제 실패 - 본인 알람 아님")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void delete_notification_fail_not_my_notification() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/notifications/{id}", notification3.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1NotificationController.class))
                .andExpect(handler().methodName("deleteNotification"))
                .andExpect(status().isForbidden());
    }
}