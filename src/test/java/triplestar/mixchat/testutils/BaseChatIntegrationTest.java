package triplestar.mixchat.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import triplestar.mixchat.global.security.jwt.AuthJwtProvider;
import triplestar.mixchat.global.ai.ChatClientChainExecutor;
import triplestar.mixchat.domain.ai.chatbot.AiChatBotService;
import triplestar.mixchat.domain.ai.systemprompt.service.AiFeedbackService;
import triplestar.mixchat.domain.chat.chat.repository.AIChatRoomRepository;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteRagService;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteDocumentRepository;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteEmbeddingService;
import triplestar.mixchat.domain.ai.userprompt.repository.UserPromptRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.DirectChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.notification.repository.NotificationRepository;
import triplestar.mixchat.global.security.CustomUserDetails;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 채팅 도메인 통합 테스트의 공통 베이스 클래스입니다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseChatIntegrationTest extends RedisTestContainer {

    // .testcontainers.properties 파일에서 reuse 설정을 활성화해야 재사용 가능
    @Container
    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withUsername("test")
            .withPassword("test")
            .withDatabaseName("mixchat_test")
            .withReuse(true);

    // MongoDB 컨테이너 설정 및 재사용 활성화
    @Container
    protected static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0")
            .withReuse(true);

    // Testcontainers 동적 속성 설정
    @DynamicPropertySource
    static void registerDataSources(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
        registry.add("spring.data.mongodb.auditing.enabled", () -> "false");

        // Redis 클라이언트 타임아웃 증가 (Testcontainers 환경에서 부하 시 타임아웃 방지)
        registry.add("spring.data.redis.timeout", () -> "5s");

        // AI Context Retriever 설정 (테스트용 기본값)
        registry.add("ai.context-retriever.sql.min", () -> "1");
        registry.add("ai.context-retriever.sql.max", () -> "10");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected UserPromptRepository userPromptRepository;

    @Autowired
    protected ChatMessageRepository chatMessageRepository;

    @Autowired
    protected GroupChatRoomRepository groupChatRoomRepository;

    @Autowired
    protected DirectChatRoomRepository directChatRoomRepository;

    @Autowired
    protected AIChatRoomRepository aiChatRoomRepository;

    @Autowired
    protected ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    protected NotificationRepository notificationRepository;

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @MockitoBean
    protected ElasticsearchTemplate elasticsearchTemplate;

    @MockitoBean
    protected AuthJwtProvider authJwtProvider;

    @MockitoBean
    protected ChatClientChainExecutor chatClientChainExecutor;

    @MockitoBean
    protected AiChatBotService aiChatBotService;

    @MockitoBean
    protected AiFeedbackService aiFeedbackService;

    @MockitoBean
    protected LearningNoteRagService learningNoteRagService;

    @MockitoBean
    protected LearningNoteDocumentRepository learningNoteDocumentRepository;

    @MockitoBean
    protected LearningNoteEmbeddingService learningNoteEmbeddingService;

    // 테스트 간 데이터 오염 방지를 위해 Redis 및 DB 데이터 초기화
    @BeforeEach
    void baseSetUp() {
        if (redisTemplate.getConnectionFactory() != null) {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        }

        // DB 데이터 초기화 (순서 중요: FK 제약조건 때문에 자식 테이블 먼저 삭제)
        notificationRepository.deleteAll(); // Notification은 Member를 참조함
        chatRoomMemberRepository.deleteAll();
        directChatRoomRepository.deleteAll();
        groupChatRoomRepository.deleteAll();
        aiChatRoomRepository.deleteAll();

        chatMessageRepository.deleteAll(); // MongoDB는 FK 없음

        userPromptRepository.deleteAll();
        memberRepository.deleteAll();

        // 봇 멤버(ID: 101) 강제 삽입 (AI 채팅방 생성 시 필수)
        jdbcTemplate.update("""
            INSERT INTO members (
                id, email, password, name, nickname, country, english_level, role, 
                membership_grade, is_blocked, is_deleted, created_at, modified_at, 
                interests, description, last_seen_at
            ) VALUES (
                101, 'bot@mixchat.com', 'dummy', 'Chat Bot', 'Chat Bot', 'KR', 'BEGINNER', 'ROLE_MEMBER',
                'BASIC', false, false, NOW(), NOW(),
                '["IT"]', 'I am a bot', NOW()
            )
        """);
    }

    // 닉네임 기반 테스트용 회원 생성 및 저장 헬퍼
    protected Member createMember(String nickname) {
        // Password 객체 생성 (public 접근 가능한 encrypt 메서드 사용)
        Password password =
                Password.encrypt(
                        "Password123", // 유효성 검사 통과용 (영문+숫자, 8자 이상)
                        new PasswordEncoder() {
                            @Override public String encode(CharSequence raw) { return raw.toString(); }
                            @Override public boolean matches(CharSequence raw, String encoded) { return true; }
                        }
                );

        Member member = Member.createMember(
                nickname + "@test.com",
                password,
                nickname, // name
                nickname, // nickname
                Country.KR,
                EnglishLevel.BEGINNER,
                java.util.List.of("IT"),
                "Description"
        );
        return memberRepository.save(member);
    }

    // Member 엔티티를 CustomUserDetails로 변환하는 헬퍼
    protected CustomUserDetails toUserDetails(Member member) {
        return new CustomUserDetails(member.getId(), member.getRole(), member.getNickname());
    }
}