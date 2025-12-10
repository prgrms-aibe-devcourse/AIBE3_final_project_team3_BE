package triplestar.mixchat.domain.miniGame.sentenceGame.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
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
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameSubmitReq;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;
import triplestar.mixchat.domain.miniGame.sentenceGame.repository.SentenceGameRepository;
import triplestar.mixchat.testutils.TestMemberFactory;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("문장 미니게임 API 테스트")
class ApiV1SentenceGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SentenceGameRepository sentenceGameRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(TestMemberFactory.createMember("testUser1"));

        sentenceGameRepository.save(SentenceGame.createSentenceGame(
                "I goed to school.",
                "I went to school.",
                List.of()
        ));
        sentenceGameRepository.save(SentenceGame.createSentenceGame(
                "She don't like apples.",
                "She doesn't like apples.",
                List.of()
        ));
        sentenceGameRepository.save(SentenceGame.createSentenceGame(
                "He go to work every day.",
                "He goes to work every day.",
                List.of()
        ));
        sentenceGameRepository.save(SentenceGame.createSentenceGame(
                "They was happy.",
                "They were happy.",
                List.of()
        ));
        sentenceGameRepository.save(SentenceGame.createSentenceGame(
                "It have two legs.",
                "It has two legs.",
                List.of()
        ));
        sentenceGameRepository.save(SentenceGame.createSentenceGame(
                "We is ready.",
                "We are ready.",
                List.of()
        ));
    }

    @Test
    @DisplayName("전체 문제 수 조회 성공")
    @WithUserDetails(value = "testUser1",userDetailsServiceBeanName = "testUserDetailsService",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getTotalCount_success() throws Exception {
        mockMvc.perform(
                        get("/api/v1/sentence-game")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("문장 게임 전체 문제 수 조회 성공"))
                .andExpect(jsonPath("$.data.totalCount").value(6));
    }

    @Test
    @DisplayName("문장 미니게임 시작 성공 - count 개수만큼 문제 반환")
    @WithUserDetails(value = "testUser1",userDetailsServiceBeanName = "testUserDetailsService",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void startGame_success() throws Exception {
        mockMvc.perform(
                        get("/api/v1/sentence-game/start")
                                .param("count", "3")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("게임 문제가 생성되었습니다."))
                .andExpect(jsonPath("$.data.questions.length()").value(3));
    }

    @Test
    @DisplayName("문장 미니게임 시작 실패 - 요구 문장이 더 많음")
    @WithUserDetails(value = "testUser1",userDetailsServiceBeanName = "testUserDetailsService",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void startGame_fail_noData() throws Exception {
        mockMvc.perform(get("/api/v1/sentence-game/start")
                        .param("count", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("정답 제출 성공 - 정답")
    @WithUserDetails(value = "testUser1",userDetailsServiceBeanName = "testUserDetailsService",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void submit_correct() throws Exception {
        SentenceGame game = sentenceGameRepository.findAll().getFirst();
        SentenceGameSubmitReq req = new SentenceGameSubmitReq(
                game.getId(),
                game.getCorrectedContent()
        );

        mockMvc.perform(
                        post("/api/v1/sentence-game/submit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("정답 확인 완료"))
                .andExpect(jsonPath("$.data.correct").value(true))
                .andExpect(jsonPath("$.data.correctedContent").value(game.getCorrectedContent()));
    }

    @Test
    @DisplayName("정답 제출 성공 - 오답")
    @WithUserDetails(value = "testUser1",userDetailsServiceBeanName = "testUserDetailsService",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void submit_wrong() throws Exception {
        SentenceGame game = sentenceGameRepository.findAll().get(0);

        SentenceGameSubmitReq req = new SentenceGameSubmitReq(
                game.getId(),
                "Wrong answer"
        );

        mockMvc.perform(
                        post("/api/v1/sentence-game/submit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("정답 확인 완료"))
                .andExpect(jsonPath("$.data.correct").value(false))
                .andExpect(jsonPath("$.data.correctedContent").value(game.getCorrectedContent()));
    }

    @Test
    @DisplayName("정답 제출 실패 - 존재하지 않는 문항 ID")
    @WithUserDetails(value = "testUser1",userDetailsServiceBeanName = "testUserDetailsService",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void submit_notFound() throws Exception {
        String invalidJson = """
            {
              "sentenceGameId": 9999,
              "userAnswer": "I went to school."
            }
            """;

        mockMvc.perform(
                        post("/api/v1/sentence-game/submit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("존재하지 않는 엔티티에 접근했습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("정답 제출 실패 - 필수 값 누락")
    @WithUserDetails(value = "testUser1",userDetailsServiceBeanName = "testUserDetailsService",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void submit_missingField_fail() throws Exception {
        String invalidJson = """
            {
              "sentenceGameId": 1
            }
            """;

        mockMvc.perform(
                        post("/api/v1/sentence-game/submit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("요청 값이 유효하지 않습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
}