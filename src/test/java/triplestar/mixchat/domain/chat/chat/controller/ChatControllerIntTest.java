package triplestar.mixchat.domain.chat.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomRepository;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    // 통합 테스트에서는 실제 Repository를 주입받아 DB 상태를 준비하고 검증합니다.
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member user1;
    private Member user2;

    @BeforeEach
    void setUp() {
        // 테스트 실행 전, 실제 DB에 테스트용 사용자를 저장합니다.
        // @Transactional에 의해 테스트가 끝나면 롤백됩니다.
        user1 = memberRepository.save(Member.builder()
                .email("user1@example.com")
                .nickname("유저1")
                .password(Password.encrypt("ValidPassword123", passwordEncoder))
                .name("유저1")
                .country(Country.SOUTH_KOREA)
                .englishLevel(EnglishLevel.BEGINNER)
                .interest("테스트")
                .description("테스트 유저 1")
                .build());
        user2 = memberRepository.save(Member.builder()
                .email("user2@example.com")
                .nickname("유저2")
                .password(Password.encrypt("ValidPassword123", passwordEncoder))
                .name("유저2")
                .country(Country.UNITED_STATES)
                .englishLevel(EnglishLevel.INTERMEDIATE)
                .interest("테스트")
                .description("테스트 유저 2")
                .build());
    }

    @Test
    @WithUserDetails(value = "user1@example.com", userDetailsServiceBeanName = "customUserDetailsService")
    @DisplayName("1:1 채팅방 생성 통합 테스트 성공")
    void createDirectRoom_integration_success() throws Exception {
        // given (준비)
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
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("1:1 채팅방 생성/조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.name").value("유저1, 유저2"));

        // then (검증) - 2. DB 상태 검증
        // 채팅방이 DB에 실제로 1개 생성되었는지 확인합니다.
        // 이것이 @SpringBootTest의 핵심입니다.
        long roomCount = chatRoomRepository.count();
        assertThat(roomCount).isEqualTo(1);
    }
}
