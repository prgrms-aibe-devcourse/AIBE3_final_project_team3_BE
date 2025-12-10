package triplestar.mixchat.domain.chat.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.testutils.RedisTestContainer;
import triplestar.mixchat.testutils.TestMemberFactory;

/**
 * 실제 DB(MySQL), Mongo, Redis(Testcontainers)와 통합된 컨트롤러 흐름 테스트.
 * 외부 스토리지(S3) 등은 호출하지 않는 시나리오만 다룹니다.
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

    @MockitoBean
    private S3Uploader s3Uploader;

    private CustomUserDetails currentUser;
    private Member user1;
    private Member user2;

    @BeforeEach
    void setUp() {
        // 테스트용 멤버 2명 생성
        memberRepository.deleteAll();
        user1 = memberRepository.save(TestMemberFactory.createMember("user1"));
        user2 = memberRepository.save(TestMemberFactory.createMember("user2"));
        currentUser = new CustomUserDetails(user1.getId(), user1.getRole(), user1.getNickname());
    }

    @Test
    @DisplayName("1:1 채팅방 생성 → 메시지 저장(DB) → 메시지 조회 및 파일 업로드 통합 시나리오")
    void directChat_endToEnd() throws Exception {
        // 1) 방 생성
        String createRoomResponse = mockMvc.perform(post("/api/v1/chats/rooms/direct")
                        .with(user(currentUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq(user2.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.user1.id").value(user1.getId()))
                .andExpect(jsonPath("$.data.user2.id").value(user2.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode roomJson = objectMapper.readTree(createRoomResponse);
        Long roomId = roomJson.path("data").path("id").asLong();

        // 2) 텍스트 메시지 DB 직접 저장 (WebSocket 대체)
        ChatMessage message = new ChatMessage(
                roomId,
                user1.getId(),
                System.currentTimeMillis(), // sequence를 timestamp로 사용하여 충돌 방지
                "hello direct msg",
                ChatMessage.MessageType.TEXT,
                ChatRoomType.DIRECT,
                false
        );
        chatMessageRepository.save(message);

        // 3) 메시지 조회 검증 - 보낸 메시지가 조회되는지 확인
        mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", roomId)
                        .with(user(currentUser))
                        .param("chatRoomType", "DIRECT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages[?(@.content == 'hello direct msg')].messageType").value("TEXT"))
                .andExpect(jsonPath("$.data.chatRoomType").value("DIRECT"));

        // 4) 파일 메시지 전송 (실제 컨트롤러 API 존재)
        // S3Uploader Mocking 설정
        String mockFileUrl = "https://s3.mock-url/test.jpg";
        given(s3Uploader.uploadFile(any(), anyString())).willReturn(mockFileUrl);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/chats/rooms/{roomId}/files", roomId)
                        .file(file)
                        .with(user(currentUser))
                        .with(csrf())
                        .param("chatRoomType", "DIRECT")
                        .param("messageType", "IMAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value(mockFileUrl));
    }
}
