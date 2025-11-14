package triplestar.mixchat.domain.member.friend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
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
import triplestar.mixchat.domain.member.friend.service.FriendshipRequestService;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.testutils.TestMemberFactory;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("친구관계 컨트롤러")
class ApiV1FriendshipControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private FriendshipRequestService friendshipRequestService;
    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(TestMemberFactory.createMember("user1"));
        member2 = memberRepository.save(TestMemberFactory.createMember("user2"));
    }

    @Test
    @DisplayName("친구요청 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_request_success() throws Exception {
        Long receiverId = member2.getId();
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/friends")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("%d".formatted(receiverId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("sendFriendRequest"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("친구요청 실패 - 자기 자신")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_request_fail() throws Exception {
        Long receiverId = member1.getId();
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/friends")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("%d".formatted(receiverId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("sendFriendRequest"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("친구요청 실패 - 중복")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_request_duplicate() throws Exception {
        Long receiverId = member2.getId();
        friendshipRequestService.sendRequest(member1.getId(), receiverId);
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/friends")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("%d".formatted(receiverId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("sendFriendRequest"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("친구요청 실패 - 중복(상대가 이미 보냄)")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_request_duplicate_opponent() throws Exception {
        friendshipRequestService.sendRequest(member2.getId(), member1.getId());
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/friends")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("%d".formatted(member2.getId()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("sendFriendRequest"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("친구요청 처리(수락) 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_accept_success() throws Exception {
        Long requestId = friendshipRequestService.sendRequest(member2.getId(), member1.getId());

        ResultActions resultActions = mvc
                .perform(
                        patch("/api/v1/members/friends/{requestId}/accept", requestId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("acceptRequest"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("친구요청 처리(거절) 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_reject_success() throws Exception {
        Long requestId = friendshipRequestService.sendRequest(member2.getId(), member1.getId());

        ResultActions resultActions = mvc
                .perform(
                        patch("/api/v1/members/friends/{requestId}/reject", requestId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("rejectRequest"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("친구요청 처리 실패 - 존재하지 않는 요청")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_process_not_found() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        patch("/api/v1/members/friends/{requestId}/reject", Long.MAX_VALUE)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("rejectRequest"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("친구요청 처리 실패 - 권한 없음")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_process_denied() throws Exception {
        Long requestId = friendshipRequestService.sendRequest(member1.getId(), member2.getId());
        ResultActions resultActions = mvc
                .perform(
                        patch("/api/v1/members/friends/{requestId}/reject", requestId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("rejectRequest"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("친구 삭제 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_delete_success() throws Exception {
        Long requestId = friendshipRequestService.sendRequest(member1.getId(), member2.getId());
        friendshipRequestService.processRequest(member2.getId(), requestId, true);

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/members/friends/{friendId}", member2.getId())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("deleteFriend"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("친구 삭제 실패 - 친구관계가 아님")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_delete_fail() throws Exception {
        friendshipRequestService.sendRequest(member1.getId(), member2.getId());

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/members/friends/{friendId}", member2.getId())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("deleteFriend"))
                .andExpect(status().isNotFound());
    }
}