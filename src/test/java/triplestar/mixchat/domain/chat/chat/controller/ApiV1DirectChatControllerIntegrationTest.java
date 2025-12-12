package triplestar.mixchat.domain.chat.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.testutils.BaseChatIntegrationTest;

class ApiV1DirectChatControllerIntegrationTest extends BaseChatIntegrationTest {

    @MockitoBean
    private S3Uploader s3Uploader;

    private Member user1;
    private Member user2;
    private Member user3;
    private CustomUserDetails user1Details;
    private CustomUserDetails user3Details;

    @BeforeEach
    void setUp() {
        // Base에서 제공하는 helper 사용
        user1 = createMember("user1");
        user2 = createMember("user2");
        user3 = createMember("user3");

        user1Details = toUserDetails(user1);
        user3Details = toUserDetails(user3);
    }

    @Test
    @DisplayName("1:1 채팅방 생성 → 메시지 저장 → 조회 → 파일 업로드 → 나가기")
    void directChat_fullEndToEnd() throws Exception {
        // 1. 채팅방 생성
        Long roomId = createDirectChatRoom(user1Details, user2.getId());

        // 2. 채팅방 목록 조회 - user1이 생성한 방이 있는지 확인
        mockMvc.perform(get("/api/v1/chats/rooms/direct")
                        .with(user(user1Details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(roomId));

        // 3. 텍스트 메시지 저장 (Base helper 사용)
        createDirectMessages(roomId, user1, 1);

        // 4. 메시지 조회 - 입장 시 자동 읽음 처리 포함
        mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", roomId)
                        .with(user(user1Details))
                        .param("chatRoomType", "DIRECT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages[0].messageType").value("TEXT"))
                .andExpect(jsonPath("$.data.chatRoomType").value("DIRECT"));

        // 5. 파일 메시지 업로드 (이미지)
        given(s3Uploader.uploadFile(any(), anyString())).willReturn("https://s3.mock/image.jpg");
        MockMultipartFile imageFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image-content".getBytes());

        mockMvc.perform(multipart("/api/v1/chats/rooms/{roomId}/files", roomId)
                        .file(imageFile)
                        .with(user(user1Details))
                        .with(csrf())
                        .param("chatRoomType", "DIRECT")
                        .param("messageType", "IMAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("https://s3.mock/image.jpg"))
                .andExpect(jsonPath("$.data.messageType").value("IMAGE"));

        // 6. 채팅방 나가기
        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}", roomId)
                        .with(user(user1Details))
                        .with(csrf())
                        .param("chatRoomType", "DIRECT"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("중복 1:1 채팅방 생성 시도 - 기존 방 반환")
    void createDirectRoom_duplicate_returnsExistingRoom() throws Exception {
        Long firstRoomId = createDirectChatRoom(user1Details, user2.getId());

        mockMvc.perform(post("/api/v1/chats/rooms/direct")
                        .with(user(user1Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDirectChatReq(user2.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(firstRoomId));
    }

    @Test
    @DisplayName("존재하지 않는 사용자와 1:1 채팅방 생성 시도 - 실패")
    void createWithNonExistentUser_fails() throws Exception {
        Long nonExistentUserId = 99999L;

        mockMvc.perform(post("/api/v1/chats/rooms/direct")
                        .with(user(user1Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDirectChatReq(nonExistentUserId))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("참여하지 않은 채팅방의 메시지 조회 시도 - 실패")
    void accessDenied_nonMember() throws Exception {
        Long roomId = createDirectChatRoom(user1Details, user2.getId());

        mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", roomId)
                        .with(user(user3Details))
                        .param("chatRoomType", "DIRECT"))
                .andExpect(status().is4xxClientError());
    }

    // Helper method for this test class
    private Long createDirectChatRoom(CustomUserDetails creator, Long partnerId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/chats/rooms/direct")
                        .with(user(creator))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDirectChatReq(partnerId))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private void createDirectMessages(Long roomId, Member sender, int count) {
        long baseSequence = System.currentTimeMillis();
        for (int i = 1; i <= count; i++) {
            ChatMessage msg = new ChatMessage(
                    roomId,
                    sender.getId(),
                    baseSequence + i,
                    "Message " + i,
                    ChatMessage.MessageType.TEXT,
                    ChatRoomType.DIRECT,
                    false
            );
            chatMessageRepository.save(msg);
        }
    }
}
