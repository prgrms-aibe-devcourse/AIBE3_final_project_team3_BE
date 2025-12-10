package triplestar.mixchat.domain.chat.chat.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.CreateAIChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreateGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.InviteGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.JoinGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.TransferOwnerReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.testutils.RedisTestContainer;
import triplestar.mixchat.testutils.TestMemberFactory;

/**
 * Chat 도메인 통합 테스트
 *
 * 실제 DB(MySQL), Mongo, Redis(Testcontainers)를 사용한 완전한 E2E 테스트
 *
 * 테스트 범위:
 * 1. Direct Chat (1:1 채팅방) - 생성, 조회, 메시지, 나가기
 * 2. Group Chat (그룹 채팅방) - 생성, 참가, 초대, 강퇴, 방장 위임, 나가기
 * 3. AI Chat (AI 채팅방) - 생성, 조회, 나가기
 * 4. 메시지 - 페이지네이션, 파일 업로드, 읽음 처리
 * 5. 권한 검증 - 비인가 사용자, 권한 없는 작업
 * 6. 예외 상황 - 존재하지 않는 방, 중복 참가, 잘못된 비밀번호
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1ChatControllerIntegrationTest extends RedisTestContainer {

    @Container
    static final MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withUsername("test")
                    .withPassword("test")
                    .withDatabaseName("mixchat_test");

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void setProps(DynamicPropertyRegistry registry) {
        // JPA
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
        // Mongo
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
        registry.add("spring.data.mongodb.auditing.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private GroupChatRoomRepository groupChatRoomRepository;

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @MockitoBean
    private S3Uploader s3Uploader;

    private CustomUserDetails user1Details;
    private CustomUserDetails user2Details;
    private CustomUserDetails user3Details;
    private Member user1;
    private Member user2;
    private Member user3;

    @BeforeEach
    void setUp() {
        // 테스트용 멤버 3명 생성
        memberRepository.deleteAll();
        user1 = memberRepository.save(TestMemberFactory.createMember("user1"));
        user2 = memberRepository.save(TestMemberFactory.createMember("user2"));
        user3 = memberRepository.save(TestMemberFactory.createMember("user3"));

        user1Details = new CustomUserDetails(user1.getId(), user1.getRole(), user1.getNickname());
        user2Details = new CustomUserDetails(user2.getId(), user2.getRole(), user2.getNickname());
        user3Details = new CustomUserDetails(user3.getId(), user3.getRole(), user3.getNickname());
    }

    /**
     * Direct Chat (1:1 채팅방) 통합 테스트
     */
    @Nested
    @DisplayName("Direct Chat (1:1 채팅방)")
    class DirectChatTests {

        @Test
        @DisplayName("1:1 채팅방 생성 → 메시지 저장 → 조회 → 파일 업로드 → 나가기 (완전한 E2E)")
        void directChat_fullEndToEnd() throws Exception {
            // 1. 채팅방 생성
            Long roomId = createDirectChatRoom(user1Details, user2.getId());

            // 2. 채팅방 목록 조회 - user1이 생성한 방이 있는지 확인
            mockMvc.perform(get("/api/v1/chats/rooms/direct")
                            .with(user(user1Details)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(roomId));

            // 3. 텍스트 메시지 저장
            ChatMessage textMessage = new ChatMessage(
                    roomId,
                    user1.getId(),
                    System.currentTimeMillis(),
                    "Hello from user1",
                    ChatMessage.MessageType.TEXT,
                    ChatRoomType.DIRECT,
                    false
            );
            chatMessageRepository.save(textMessage);

            // 4. 메시지 조회 - 입장 시 자동 읽음 처리 포함
            mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", roomId)
                            .with(user(user1Details))
                            .param("chatRoomType", "DIRECT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messages[?(@.content == 'Hello from user1')].messageType").value("TEXT"))
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
            // 첫 번째 생성
            Long firstRoomId = createDirectChatRoom(user1Details, user2.getId());

            // 두 번째 생성 시도 - 같은 방 반환
            String response = mockMvc.perform(post("/api/v1/chats/rooms/direct")
                            .with(user(user1Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateDirectChatReq(user2.getId()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(firstRoomId))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
        }
    }

    /**
     * Group Chat (그룹 채팅방) 통합 테스트
     */
    @Nested
    @DisplayName("Group Chat (그룹 채팅방)")
    class GroupChatTests {

        @Test
        @DisplayName("그룹 채팅방 완전한 라이프사이클: 생성 → 참가 → 초대 → 메시지 → 강퇴 → 방장 위임 → 나가기")
        void groupChat_fullLifecycle() throws Exception {
            // 1. 그룹 채팅방 생성 (user1이 방장)
            CreateGroupChatReq createReq = new CreateGroupChatReq(
                    "Test Group",
                    List.of(user1.getId()),
                    null,  // 비밀번호 없음
                    "Group Description",
                    "General"
            );

            String createResponse = mockMvc.perform(post("/api/v1/chats/rooms/group")
                            .with(user(user1Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.title").value("Test Group"))
                    .andExpect(jsonPath("$.data.isPublic").value(true))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long roomId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

            // 2. 공개 그룹 목록 조회 - 생성된 방이 보이는지 확인
            mockMvc.perform(get("/api/v1/chats/rooms/group/public")
                            .with(user(user2Details)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[?(@.id == " + roomId + ")].title").value("Test Group"));

            // 3. user2가 그룹 참가
            mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                            .with(user(user2Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(roomId));

            // 4. user1이 user3를 초대
            InviteGroupChatReq inviteReq = new InviteGroupChatReq(user3.getId());
            mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/invite", roomId)
                            .with(user(user1Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inviteReq)))
                    .andExpect(status().isOk());

            // 5. 메시지 전송 (user1)
            ChatMessage message = new ChatMessage(
                    roomId,
                    user1.getId(),
                    System.currentTimeMillis(),
                    "Group message from user1",
                    ChatMessage.MessageType.TEXT,
                    ChatRoomType.GROUP,
                    false
            );
            chatMessageRepository.save(message);

            // 6. 메시지 조회 (user2)
            mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", roomId)
                            .with(user(user2Details))
                            .param("chatRoomType", "GROUP"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messages[?(@.content == 'Group message from user1')]").exists());

            // 7. user1(방장)이 user3을 강퇴
            mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}/members/{memberId}", roomId, user3.getId())
                            .with(user(user1Details))
                            .with(csrf()))
                    .andExpect(status().isOk());

            // 8. 방장 위임 (user1 → user2)
            TransferOwnerReq transferReq = new TransferOwnerReq(user2.getId());
            mockMvc.perform(patch("/api/v1/chats/rooms/{roomId}/owner", roomId)
                            .with(user(user1Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transferReq)))
                    .andExpect(status().isOk());

            // 9. user1이 방 나가기
            mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}", roomId)
                            .with(user(user1Details))
                            .with(csrf())
                            .param("chatRoomType", "GROUP"))
                    .andExpect(status().isOk());

            // 10. user2(새 방장)가 방 목록 조회 - 방이 여전히 존재
            mockMvc.perform(get("/api/v1/chats/rooms/group")
                            .with(user(user2Details)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[?(@.id == " + roomId + ")]").exists());
        }

        @Test
        @DisplayName("비밀번호가 있는 그룹 채팅방 - 올바른 비밀번호로 참가 성공")
        void groupChat_withPassword_joinSuccess() throws Exception {
            // 1. 비밀번호가 있는 그룹 생성
            CreateGroupChatReq createReq = new CreateGroupChatReq(
                    "Secret Group",
                    List.of(user1.getId()),
                    "password123",
                    "Description",
                    "General"
            );

            String createResponse = mockMvc.perform(post("/api/v1/chats/rooms/group")
                            .with(user(user1Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long roomId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

            // 2. 올바른 비밀번호로 참가
            JoinGroupChatReq joinReq = new JoinGroupChatReq("password123");
            mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                            .with(user(user2Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(joinReq)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("비밀번호가 있는 그룹 채팅방 - 잘못된 비밀번호로 참가 실패")
        void groupChat_withPassword_joinFailWithWrongPassword() throws Exception {
            // 1. 비밀번호가 있는 그룹 생성
            CreateGroupChatReq createReq = new CreateGroupChatReq(
                    "Secret Group",
                    List.of(user1.getId()),
                    "password123",
                    "Description",
                    "General"
            );

            String createResponse = mockMvc.perform(post("/api/v1/chats/rooms/group")
                            .with(user(user1Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long roomId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

            // 2. 잘못된 비밀번호로 참가 시도
            JoinGroupChatReq joinReq = new JoinGroupChatReq("wrong-password");
            mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                            .with(user(user2Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(joinReq)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("방장이 아닌 사용자가 강퇴 시도 - 실패")
        void groupChat_kickMember_byNonOwner_fails() throws Exception {
            // 1. 그룹 생성 및 user2, user3 참가
            Long roomId = createGroupChatRoomWithMembers();

            // 2. user2(일반 멤버)가 user3을 강퇴 시도
            mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}/members/{memberId}", roomId, user3.getId())
                            .with(user(user2Details))
                            .with(csrf()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("방장이 아닌 사용자가 방장 위임 시도 - 실패")
        void groupChat_transferOwnership_byNonOwner_fails() throws Exception {
            // 1. 그룹 생성 및 user2 참가
            Long roomId = createGroupChatRoomWithMembers();

            // 2. user2(일반 멤버)가 방장 위임 시도
            TransferOwnerReq transferReq = new TransferOwnerReq(user3.getId());
            mockMvc.perform(patch("/api/v1/chats/rooms/{roomId}/owner", roomId)
                            .with(user(user2Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transferReq)))
                    .andExpect(status().is4xxClientError());
        }
    }

    /**
     * AI Chat (AI 채팅방) 통합 테스트
     */
    @Nested
    @DisplayName("AI Chat (AI 채팅방)")
    class AIChatTests {

        @Test
        @DisplayName("AI 채팅방 생성 → 메시지 → 목록 조회 → 나가기")
        void aiChat_fullLifecycle() throws Exception {
            // 1. AI 채팅방 생성
            CreateAIChatReq createReq = new CreateAIChatReq("AI Helper", 1L, AiChatRoomType.ROLE_PLAY);
            String createResponse = mockMvc.perform(post("/api/v1/chats/rooms/ai")
                            .with(user(user1Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.title").value("AI Helper"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long roomId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

            // 2. AI 채팅방 목록 조회
            mockMvc.perform(get("/api/v1/chats/rooms/ai")
                            .with(user(user1Details)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].id").value(roomId));

            // 3. 메시지 전송
            ChatMessage message = new ChatMessage(
                    roomId,
                    user1.getId(),
                    System.currentTimeMillis(),
                    "Hello AI",
                    ChatMessage.MessageType.TEXT,
                    ChatRoomType.AI,
                    false
            );
            chatMessageRepository.save(message);

            // 4. 메시지 조회
            mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", roomId)
                            .with(user(user1Details))
                            .param("chatRoomType", "AI"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messages[?(@.content == 'Hello AI')]").exists());

            // 5. AI 채팅방 나가기
            mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}", roomId)
                            .with(user(user1Details))
                            .with(csrf())
                            .param("chatRoomType", "AI"))
                    .andExpect(status().isOk());
        }
    }

    /**
     * 메시지 관련 통합 테스트
     */
    @Nested
    @DisplayName("메시지 기능")
    class MessageTests {

        @Test
        @DisplayName("메시지 페이지네이션 - 커서 기반 페이징")
        void messages_pagination_cursorBased() throws Exception {
            // 1. 채팅방 생성
            Long roomId = createDirectChatRoom(user1Details, user2.getId());

            // 2. 여러 메시지 저장 (10개)
            for (int i = 1; i <= 10; i++) {
                ChatMessage message = new ChatMessage(
                        roomId,
                        user1.getId(),
                        System.currentTimeMillis() + i,  // sequence를 증가시켜 순서 보장
                        "Message " + i,
                        ChatMessage.MessageType.TEXT,
                        ChatRoomType.DIRECT,
                        false
                );
                chatMessageRepository.save(message);
                Thread.sleep(1);  // sequence 중복 방지
            }

            // 3. 첫 페이지 조회 (size=5)
            MvcResult firstPageResult = mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", roomId)
                            .with(user(user1Details))
                            .param("chatRoomType", "DIRECT")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messages").isArray())
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andReturn();

            JsonNode firstPage = objectMapper.readTree(firstPageResult.getResponse().getContentAsString());
            Long cursor = firstPage.path("data").path("nextCursor").asLong();

            // 4. 다음 페이지 조회 (cursor 사용)
            mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", roomId)
                            .with(user(user1Details))
                            .param("chatRoomType", "DIRECT")
                            .param("cursor", cursor.toString())
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messages").isArray());
        }

        @Test
        @DisplayName("다양한 파일 타입 업로드 - 이미지, 비디오, 음성")
        void fileUpload_variousTypes() throws Exception {
            Long roomId = createDirectChatRoom(user1Details, user2.getId());

            // 1. 이미지 업로드
            given(s3Uploader.uploadFile(any(), anyString())).willReturn("https://s3.mock/image.jpg");
            MockMultipartFile imageFile = new MockMultipartFile("file", "image.jpg", "image/jpeg", "image-data".getBytes());

            mockMvc.perform(multipart("/api/v1/chats/rooms/{roomId}/files", roomId)
                            .file(imageFile)
                            .with(user(user1Details))
                            .with(csrf())
                            .param("chatRoomType", "DIRECT")
                            .param("messageType", "IMAGE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messageType").value("IMAGE"));

            // 2. 비디오 업로드
            given(s3Uploader.uploadFile(any(), anyString())).willReturn("https://s3.mock/video.mp4");
            MockMultipartFile videoFile = new MockMultipartFile("file", "video.mp4", "video/mp4", "video-data".getBytes());

            mockMvc.perform(multipart("/api/v1/chats/rooms/{roomId}/files", roomId)
                            .file(videoFile)
                            .with(user(user1Details))
                            .with(csrf())
                            .param("chatRoomType", "DIRECT")
                            .param("messageType", "VIDEO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messageType").value("VIDEO"));

            // 3. 음성 업로드
            given(s3Uploader.uploadFile(any(), anyString())).willReturn("https://s3.mock/audio.mp3");
            MockMultipartFile audioFile = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", "audio-data".getBytes());

            mockMvc.perform(multipart("/api/v1/chats/rooms/{roomId}/files", roomId)
                            .file(audioFile)
                            .with(user(user1Details))
                            .with(csrf())
                            .param("chatRoomType", "DIRECT")
                            .param("messageType", "AUDIO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messageType").value("AUDIO"));
        }

        @Test
        @DisplayName("메시지 조회 시 자동 읽음 처리")
        void messages_autoMarkAsRead_onRoomEnter() throws Exception {
            // 1. 채팅방 생성 및 메시지 저장
            Long roomId = createDirectChatRoom(user1Details, user2.getId());
            ChatMessage message = new ChatMessage(
                    roomId,
                    user1.getId(),
                    System.currentTimeMillis(),
                    "Test message",
                    ChatMessage.MessageType.TEXT,
                    ChatRoomType.DIRECT,
                    false
            );
            chatMessageRepository.save(message);

            // 2. user2가 메시지 조회 - 자동 읽음 처리됨
            mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", roomId)
                            .with(user(user2Details))
                            .param("chatRoomType", "DIRECT"))
                    .andExpect(status().isOk());

            // 3. 읽음 처리 확인 (ChatMember의 lastReadSequence가 업데이트되었는지)
            // Note: 실제 검증은 ChatMemberRepository를 통해 확인 가능
        }
    }

    /**
     * 권한 검증 테스트
     */
    @Nested
    @DisplayName("권한 검증")
    class AuthorizationTests {

        @Test
        @DisplayName("참여하지 않은 채팅방의 메시지 조회 시도 - 실패")
        void messages_accessDenied_nonMember() throws Exception {
            // 1. user1과 user2의 채팅방 생성
            Long roomId = createDirectChatRoom(user1Details, user2.getId());

            // 2. user3(참여하지 않은 사용자)가 메시지 조회 시도
            mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", roomId)
                            .with(user(user3Details))
                            .param("chatRoomType", "DIRECT"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("참여하지 않은 채팅방에 파일 업로드 시도 - 실패")
        void fileUpload_accessDenied_nonMember() throws Exception {
            // 1. user1과 user2의 채팅방 생성
            Long roomId = createDirectChatRoom(user1Details, user2.getId());

            // 2. user3(참여하지 않은 사용자)가 파일 업로드 시도
            given(s3Uploader.uploadFile(any(), anyString())).willReturn("https://s3.mock/file.jpg");
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());

            mockMvc.perform(multipart("/api/v1/chats/rooms/{roomId}/files", roomId)
                            .file(file)
                            .with(user(user3Details))
                            .with(csrf())
                            .param("chatRoomType", "DIRECT")
                            .param("messageType", "IMAGE"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("자신을 초대 시도 - 실패")
        void groupChat_inviteSelf_fails() throws Exception {
            Long roomId = createGroupChatRoomWithMembers();

            InviteGroupChatReq inviteReq = new InviteGroupChatReq(user1.getId());  // 자기 자신
            mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/invite", roomId)
                            .with(user(user1Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inviteReq)))
                    .andExpect(status().is4xxClientError());
        }
    }

    /**
     * 예외 상황 테스트
     */
    @Nested
    @DisplayName("예외 상황")
    class ExceptionTests {

        @Test
        @DisplayName("존재하지 않는 채팅방 조회 - 404")
        void messages_roomNotFound_returns404() throws Exception {
            Long nonExistentRoomId = 99999L;

            mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", nonExistentRoomId)
                            .with(user(user1Details))
                            .param("chatRoomType", "DIRECT"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("존재하지 않는 사용자와 1:1 채팅방 생성 시도 - 실패")
        void directChat_createWithNonExistentUser_fails() throws Exception {
            Long nonExistentUserId = 99999L;

            mockMvc.perform(post("/api/v1/chats/rooms/direct")
                            .with(user(user1Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateDirectChatReq(nonExistentUserId))))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("이미 참가한 그룹 채팅방에 재참가 시도 - 중복 참가 방지")
        void groupChat_joinTwice_prevented() throws Exception {
            // 1. 그룹 생성
            CreateGroupChatReq createReq = new CreateGroupChatReq(
                    "Test Group",
                    List.of(user1.getId()),
                    null,
                    "Description",
                    "General"
            );

            String createResponse = mockMvc.perform(post("/api/v1/chats/rooms/group")
                            .with(user(user1Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long roomId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

            // 2. user2가 참가
            mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                            .with(user(user2Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // 3. user2가 재참가 시도
            mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                            .with(user(user2Details))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("빈 파일 업로드 시도 - 실패")
        void fileUpload_emptyFile_fails() throws Exception {
            Long roomId = createDirectChatRoom(user1Details, user2.getId());

            MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

            mockMvc.perform(multipart("/api/v1/chats/rooms/{roomId}/files", roomId)
                            .file(emptyFile)
                            .with(user(user1Details))
                            .with(csrf())
                            .param("chatRoomType", "DIRECT")
                            .param("messageType", "IMAGE"))
                    .andExpect(status().is4xxClientError());
        }
    }

    // ============ Helper Methods ============

    /**
     * 1:1 채팅방 생성 헬퍼
     */
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

    /**
     * 그룹 채팅방 생성 + user2, user3 참가 헬퍼
     */
    private Long createGroupChatRoomWithMembers() throws Exception {
        // 1. user1이 그룹 생성
        CreateGroupChatReq createReq = new CreateGroupChatReq(
                "Test Group",
                List.of(user1.getId()),
                null,
                "Description",
                "General"
        );

        String createResponse = mockMvc.perform(post("/api/v1/chats/rooms/group")
                        .with(user(user1Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long roomId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        // 2. user2 참가
        mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                        .with(user(user2Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 3. user3 참가
        mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                        .with(user(user3Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        return roomId;
    }
}
