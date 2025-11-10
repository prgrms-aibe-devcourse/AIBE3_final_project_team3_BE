package triplestar.mixchat.domain.member.friend.controller;

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
        member1 = memberRepository.saveAndFlush(TestMemberFactory.createMember("user1"));
        member2 = memberRepository.saveAndFlush(TestMemberFactory.createMember("user2"));
    }

    @Test
    @DisplayName("친구요청 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_request_success() throws Exception {
        Long receiverId = member2.getId();
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/member/friends")
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
    @DisplayName("친구요청 수락")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_accept_success() throws Exception {
        Long requestId = friendshipRequestService.sendRequest(member2.getId(), member1.getId());

        ResultActions resultActions = mvc
                .perform(
                        patch("/api/v1/member/friends/{requestId}/accept", requestId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("acceptRequest"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("친구요청 거절")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void friend_reject_success() throws Exception {
        Long requestId = friendshipRequestService.sendRequest(member2.getId(), member1.getId());

        ResultActions resultActions = mvc
                .perform(
                        patch("/api/v1/member/friends/{requestId}/reject", requestId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1FriendshipController.class))
                .andExpect(handler().methodName("rejectRequest"))
                .andExpect(status().isOk());
    }
}