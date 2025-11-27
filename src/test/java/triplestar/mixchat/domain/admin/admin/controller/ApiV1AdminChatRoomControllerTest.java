package triplestar.mixchat.domain.admin.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.transaction.Transactional;
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
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.testutils.TestMemberFactory;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("관리자 채팅방 강제 폐쇄 API 테스트")
public class ApiV1AdminChatRoomControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    GroupChatRoomRepository groupChatRoomRepository;

    @Autowired
    ChatRoomMemberRepository chatMemberRepository;

    private Member admin;
    private Member user1;
    private Member user2;
    private GroupChatRoom room;

    @BeforeEach
    void setUp() {
        admin = memberRepository.save(TestMemberFactory.createAdmin("adminUser"));
        user1 = memberRepository.save(TestMemberFactory.createMember("userA"));
        user2 = memberRepository.save(TestMemberFactory.createMember("userB"));

        // 🔥 새로운 create() 시그니처 반영
        room = groupChatRoomRepository.save(
                GroupChatRoom.create(
                        "Test Room",             // name
                        "Room description",      // description
                        "General topic",         // topic
                        null,                    // password
                        user1                    // owner
                )
        );

        // 멤버 추가
        chatMemberRepository.save(ChatMember.create(room, user1, null));
        chatMemberRepository.save(ChatMember.create(room, user2, null));
    }

    @Test
    @DisplayName("관리자 - 그룹 채팅방 강제 폐쇄 성공")
    @WithUserDetails(value = "adminUser",userDetailsServiceBeanName = "testUserDetailsService",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void forceCloseRoom_success() throws Exception {

        Long roomId = room.getId();

        mockMvc.perform(delete("/api/v1/admin/chat-rooms/" + roomId)
                        .param("roomType", "GROUP")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("채팅방이 강제로 폐쇄되었습니다."));

        boolean exists = groupChatRoomRepository.existsById(roomId);
        assertThat(exists).isFalse();

        long memberCount = chatMemberRepository.countByChatRoomId(roomId);
        assertThat(memberCount).isZero();
    }

    @Test
    @DisplayName("관리자 - 존재하지 않는 방 강제 폐쇄 시 BadRequest")
    @WithUserDetails(
            value = "adminUser",
            userDetailsServiceBeanName = "testUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void forceCloseRoom_notFound() throws Exception {

        mockMvc.perform(delete("/api/v1/admin/chat-rooms/999999")
                        .param("roomType", "GROUP"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("USER 권한은 강제 폐쇄 불가 → 403 Forbidden")
    @WithUserDetails(
            value = "userA",
            userDetailsServiceBeanName = "testUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void forceCloseRoom_forbidden() throws Exception {

        Long roomId = room.getId();

        mockMvc.perform(delete("/api/v1/admin/chat-rooms/" + roomId)
                        .param("roomType", "GROUP"))
                .andExpect(status().isForbidden());
    }
}
