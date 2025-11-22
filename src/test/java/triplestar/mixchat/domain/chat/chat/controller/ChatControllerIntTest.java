package triplestar.mixchat.domain.chat.chat.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.cache.ChatAuthCacheService;

// NOTE : 테스트 임시 비활성화
@ImportAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class,
        MongoReactiveAutoConfiguration.class
})
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("채팅 컨트롤러 통합 테스트")
class ChatControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean // 캐시 서비스는 실제 Redis에 의존하므로 테스트 시에는 Mock 객체로 대체합니다.
    private ChatAuthCacheService chatAuthCacheService;

    // 통합 테스트에서는 실제 Repository를 주입받아 DB 상태를 준비하고 검증합니다.
    @Autowired
    private MemberRepository memberRepository;

//    @Autowired
//    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member user1;
    private Member user2;

    @BeforeEach
    void setUp() {
        // 테스트 실행 전, 실제 DB에 테스트용 사용자를 저장합니다.
        // @Transactional에 의해 테스트가 끝나면 롤백됩니다.
        // TestFactoryMember도 활용 가능합니다.
        user1 = memberRepository.save(Member.createMember(
                "user1@example.com", Password.encrypt("ValidPassword123", passwordEncoder),
                "유저1", "유저1", Country.KR, EnglishLevel.BEGINNER,
                List.of("테스트"), "테스트 유저 1"));
        user2 = memberRepository.save(Member.createMember(
                "user2@example.com", Password.encrypt("ValidPassword123", passwordEncoder),
                "유저2", "유저2", Country.UK, EnglishLevel.INTERMEDIATE,
                List.of("테스트"), "테스트 유저 2"));
    }

    //    @Test
    @WithUserDetails(value = "유저1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("1:1 채팅방 생성 통합 테스트 성공")
    void createDirectRoom_integration_success() throws Exception {
        // given (준비)
        // 이 테스트에서는 캐시 로직을 검증하는 것이 아니므로, 캐시 확인은 항상 통과하도록 설정합니다.
        given(chatAuthCacheService.isMember(anyLong(), anyLong())).willReturn(true);

        // user1, user2는 @BeforeEach에서 실제 DB에 저장되었습니다.
        CreateDirectChatReq requestDto = new CreateDirectChatReq(user2.getId());

        // when (실행)
        // user1로 인증된 상태에서 user2와의 채팅방 생성을 요청합니다.
        mockMvc.perform(post("/api/v1/chats/rooms/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())

                // then (검증) - 1. API 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("1:1 채팅방 생성/조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.name").value("유저1, 유저2"));

        // then (검증) - 2. DB 상태 검증
        // 채팅방이 DB에 실제로 1개 생성되었는지 확인합니다.
        // 이것이 @SpringBootTest의 핵심입니다.
//        long roomCount = chatRoomRepository.count();
//        assertThat(roomCount).isEqualTo(1);
    }
}
