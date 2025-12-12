package triplestar.mixchat.domain.chat.chat.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.CreateGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.InviteGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.JoinGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.TransferOwnerReq;
import triplestar.mixchat.domain.chat.chat.dto.UpdateGroupChatPasswordReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.testutils.BaseChatIntegrationTest;

class ApiV1GroupChatControllerIntegrationTest extends BaseChatIntegrationTest {

    @MockitoBean
    private S3Uploader s3Uploader;

    private Member user1;
    private Member user2;
    private Member user3;
    private CustomUserDetails user1Details;
    private CustomUserDetails user2Details;

    @BeforeEach
    void setUp() {
        // FK 제약조건 준수: 그룹 채팅방 -> 멤버 순으로 삭제
        groupChatRoomRepository.deleteAll();
        memberRepository.deleteAll();
        user1 = createMember("user1"); // Owner
        user2 = createMember("user2");
        user3 = createMember("user3");

        user1Details = toUserDetails(user1);
        user2Details = toUserDetails(user2);
    }

    @Test
    @DisplayName("그룹 채팅방 생성 -> 참가 -> 메시지 -> 초대 -> 강퇴 -> 방장 위임 -> 나가기")
    void groupChat_fullLifecycle() throws Exception {
        // 1. 그룹 생성
        Long roomId = createGroupChatRoom(user1Details, "Test Group", null);

        // 2. user2 참가
        mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                        .with(user(user2Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 3. 메시지 전송 (helper 사용)
        createGroupMessages(roomId, user1, 1);

        // 4. user1이 user3 초대
        InviteGroupChatReq inviteReq = new InviteGroupChatReq(user3.getId());
        mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/invite", roomId)
                        .with(user(user1Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteReq)))
                .andExpect(status().isOk());

        // 5. user1이 user3 강퇴
        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}/members/{memberId}", roomId, user3.getId())
                        .with(user(user1Details))
                        .with(csrf()))
                .andExpect(status().isOk());

        // 6. 방장 위임 (user1 -> user2)
        TransferOwnerReq transferReq = new TransferOwnerReq(user2.getId());
        mockMvc.perform(patch("/api/v1/chats/rooms/{roomId}/owner", roomId)
                        .with(user(user1Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferReq)))
                .andExpect(status().isOk());

        // 7. user1(구 방장) 나가기
        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}", roomId)
                        .with(user(user1Details))
                        .with(csrf())
                        .param("chatRoomType", "GROUP"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 변경 시나리오: 생성 -> 변경 -> 변경된 비번으로 참가")
    void groupChat_passwordUpdate() throws Exception {
        // 1. 비밀번호 없는 방 생성
        Long roomId = createGroupChatRoom(user1Details, "Password Test", null);

        // 2. 비밀번호 설정 ("newPass")
        UpdateGroupChatPasswordReq updateReq = new UpdateGroupChatPasswordReq("newPass");
        mockMvc.perform(patch("/api/v1/chats/rooms/group/{roomId}/password", roomId)
                        .with(user(user1Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // 3. 틀린 비번으로 참가 시도 -> 실패
        JoinGroupChatReq wrongPassReq = new JoinGroupChatReq("wrong");
        mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                        .with(user(user2Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPassReq)))
                .andExpect(status().is4xxClientError());

        // 4. 맞는 비번으로 참가 -> 성공
        JoinGroupChatReq correctPassReq = new JoinGroupChatReq("newPass");
        mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                        .with(user(user2Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctPassReq)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("방장이 방을 나가면 자동으로 다음 멤버에게 방장 위임")
    void ownerLeaves_autoTransfer() throws Exception {
        // 1. 방 생성 (user1)
        Long roomId = createGroupChatRoom(user1Details, "Auto Transfer", null);

        // 2. user2 참가
        mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                        .with(user(user2Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 3. user1(방장) 나감
        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}", roomId)
                        .with(user(user1Details))
                        .with(csrf())
                        .param("chatRoomType", "GROUP"))
                .andExpect(status().isOk());

        // 4. user2가 방장 권한으로 무언가를 수행 (예: 비번 변경) -> 성공해야 함
        UpdateGroupChatPasswordReq req = new UpdateGroupChatPasswordReq("1234");
        mockMvc.perform(patch("/api/v1/chats/rooms/group/{roomId}/password", roomId)
                        .with(user(user2Details)) // user2가 요청
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("자신을 강퇴 시도 -> 실패")
    void kickSelf_fails() throws Exception {
        Long roomId = createGroupChatRoom(user1Details, "Kick Self", null);

        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}/members/{memberId}", roomId, user1.getId())
                        .with(user(user1Details))
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("자신을 초대 시도 -> 실패")
    void inviteSelf_fails() throws Exception {
        Long roomId = createGroupChatRoom(user1Details, "Invite Self", null);

        InviteGroupChatReq req = new InviteGroupChatReq(user1.getId());
        mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/invite", roomId)
                        .with(user(user1Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    // Helper
    private Long createGroupChatRoom(CustomUserDetails creator, String title, String password) throws Exception {
        CreateGroupChatReq req = new CreateGroupChatReq(
                title,
                List.of(creator.getId()),
                password,
                "Description",
                "General"
        );

        String response = mockMvc.perform(post("/api/v1/chats/rooms/group")
                        .with(user(creator))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private void createGroupMessages(Long roomId, Member sender, int count) {
        long baseSequence = System.currentTimeMillis();
        for (int i = 1; i <= count; i++) {
            ChatMessage msg = new ChatMessage(
                    roomId,
                    sender.getId(),
                    baseSequence + i,
                    "Group message " + i,
                    ChatMessage.MessageType.TEXT,
                    ChatRoomType.GROUP,
                    false
            );
            chatMessageRepository.save(msg);
        }
    }
}
