package triplestar.mixchat.domain.chat.chat.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import triplestar.mixchat.domain.ai.translation.dto.AiFeedbackReq;
import triplestar.mixchat.domain.ai.userprompt.entity.UserPrompt;
import triplestar.mixchat.domain.ai.userprompt.repository.UserPromptRepository;
import triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.CreateAIChatReq;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.testutils.BaseChatIntegrationTest;
import triplestar.mixchat.testutils.TestMemberFactory;

@DisplayName("AI 채팅방 통합 테스트")
class ApiV1AIChatControllerIntegrationTest extends BaseChatIntegrationTest {

    private Member user1;
    private CustomUserDetails user1Details;
    @Autowired UserPromptRepository userPromptRepository;
    private Long personaId;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        aiChatRoomRepository.deleteAll();
        memberRepository.deleteAll();
        userPromptRepository.deleteAll();

        seedBot1000();

        user1 = memberRepository.save(TestMemberFactory.createMember("user1"));

        // AI 채팅방에서 사용할 UserPrompt 생성
        UserPrompt persona = userPromptRepository.save(
                UserPrompt.create(
                        user1,
                        "AI Helper Persona",
                        "You are a helpful tutor.",
                        "CUSTOM"
                )
        );
        user1Details = toUserDetails(user1);

        personaId = persona.getId();
    }

    private void seedBot1000() {
        // 이미 1000 있으면 스킵
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM members WHERE id = 1000",
                Integer.class
        );
        if (exists != null && exists > 0) return;

        // ⚠️ members 테이블이 비어있어야 첫 insert가 1000이 됨
        Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM members", Integer.class);
        if (cnt != null && cnt != 0) {
            throw new IllegalStateException(
                    "members 테이블이 비어있지 않아 bot을 id=1000로 만들 수 없습니다. " +
                            "setUp에서 memberRepository.deleteAll()이 먼저 수행되는지 확인하세요."
            );
        }

        // MySQL 전용: 다음 insert id를 1000로 맞춘다
        jdbcTemplate.execute("ALTER TABLE members AUTO_INCREMENT = 1000");

        // 너가 준 SQL 그대로 insert
        jdbcTemplate.update("""
        INSERT INTO members (email, password, name, nickname, country,
                             interests, english_level, description, role,
                             membership_grade, last_seen_at, is_blocked, blocked_at,
                             is_deleted, deleted_at, block_reason, profile_image_url,
                             created_at, modified_at)
        VALUES
        ('aichatbot@bot.com', 'botpassword',
         'chatbot', 'chatbot', 'KR',
         '["🎮 sandbox", "🍗 chicken"]',
         'ADVANCED', 'chatbot', 'ROLE_BOT', 'PREMIUM',
         NOW(), FALSE, NULL, FALSE, NULL, NULL, NULL, NOW(), NOW())
        """);

        // 안전 체크
        Integer ok = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM members WHERE id = 1000",
                Integer.class
        );
        if (ok == null || ok == 0) {
            throw new IllegalStateException("봇 생성은 됐지만 id=1000이 아닙니다. AUTO_INCREMENT 적용이 안 된 환경일 수 있습니다.");
        }
    }

    @Test
    @DisplayName("AI 채팅방 생성 -> 조회 -> 나가기")
    void aiChat_lifecycle() throws Exception {
        System.out.println("DB user1 id = " + user1.getId());
        System.out.println("principal id = " + user1Details.getId());
        // 1. 생성
        CreateAIChatReq createReq = new CreateAIChatReq("AI Helper", personaId, AiChatRoomType.ROLE_PLAY);

        String response = mockMvc.perform(post("/api/v1/chats/rooms/ai")
                        .with(user(user1Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long roomId = objectMapper.readTree(response).path("data").path("id").asLong();

        // 2. 조회
        mockMvc.perform(get("/api/v1/chats/rooms/ai")
                .with(user(user1Details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(roomId));

        // 3. 나가기
        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}", roomId)
                        .with(user(user1Details))
                        .with(csrf())
                        .param("chatRoomType", ChatRoomType.AI.name()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AI 피드백 요청 테스트")
    void aiFeedback_analyze() throws Exception {
        // AI 피드백은 채팅방 생성 여부와 관계없이 독립적으로 동작할 수도 있지만,
        // 보통 특정 상황극이 끝난 후 요청하므로 가상의 데이터를 보낸다.
        AiFeedbackReq req = new AiFeedbackReq("everyone 반갑습니다!", "Nice to meet you, everyone!");

        mockMvc.perform(post("/api/v1/chats/feedback")
                        .with(user(user1Details))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk()) // Mock 처리가 안되어있어 실제 AI를 호출하거나 예외가 날 수 있음.
                                            // 주의: 실제 외부 API 호출이 있다면 @MockitoBean으로 서비스 Mocking이 필요할 수 있음.
                .andExpect(jsonPath("$.msg").exists());
//                .andExpect(jsonPath("$.data.correctedContent").value("Nice to meet you, everyone!"))
//                .andExpect(jsonPath("$.data.feedback[0].tag").value("TRANSLATION"));
    }
}
