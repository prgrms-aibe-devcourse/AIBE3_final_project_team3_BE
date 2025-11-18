package triplestar.mixchat.domain.admin.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.web.servlet.MvcResult;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;
import triplestar.mixchat.domain.miniGame.sentenceGame.repository.SentenceGameRepository;
import triplestar.mixchat.testutils.TestMemberFactory;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("관리자 미니게임 API 테스트")
class ApiV1AdminSentenceGameControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    LearningNoteRepository learningNoteRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SentenceGameRepository sentenceGameRepository;

    private Member testAdmin;
    private Member testMember;

    @BeforeEach
    void setUp() {
        testAdmin = memberRepository.save(TestMemberFactory.createAdmin("testAdmin"));
        testMember = memberRepository.save(TestMemberFactory.createMember("testMember"));

    }

    @Test
    @DisplayName("관리자 미니게임 문장 등록 성공")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createMiniGame_success() throws Exception {

        AdminSentenceGameCreateReq req = new AdminSentenceGameCreateReq(
                "I goed to school.",
                "I went to school."
        );

        MvcResult result = mockMvc.perform(
                        post("/api/v1/admin/sentence-game")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("미니게임 문장이 등록되었습니다."))
                .andExpect(jsonPath("$.data").isNumber())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        Number rawId = JsonPath.read(responseBody, "$.data");
        Long generatedId = rawId.longValue();

        assertThat(generatedId).isNotNull();

        SentenceGame saved = sentenceGameRepository.findById(generatedId)
                .orElseThrow(() -> new AssertionError("DB에 저장된 문장을 찾을 수 없음"));

        assertThat(saved.getOriginalContent()).isEqualTo("I goed to school.");
        assertThat(saved.getCorrectedContent()).isEqualTo("I went to school.");
    }

    @Test
    @DisplayName("미니게임 등록 실패 - Validation 오류 발생(originalContent 빈값)")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createMiniGame_validation_fail() throws Exception {

        AdminSentenceGameCreateReq req = new AdminSentenceGameCreateReq(
                "", // invalid
                "I went to school."
        );

        mockMvc.perform(
                        post("/api/v1/admin/sentence-game")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("미니게임 등록 실패 - 관리자 권한이 아니면 403")
    @WithUserDetails(value = "testMember", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createMiniGame_forbidden() throws Exception {

        AdminSentenceGameCreateReq req = new AdminSentenceGameCreateReq(
                "I goed",
                "I went"
        );

        mockMvc.perform(
                        post("/api/v1/admin/sentence-game")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("미니게임 등록용 전체 학습노트 목록 조회 성공 ")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getSentenceGameNoteList_success() throws Exception {

        // --- given: 학습노트 2개 저장 ---
        LearningNote note1 = learningNoteRepository.save(
                LearningNote.create(
                        testMember,
                        "I goed to school.",
                        "I went to school."
                )
        );

        LearningNote note2 = learningNoteRepository.save(
                LearningNote.create(
                        testMember,
                        "She don't like apples.",
                        "She doesn't like apples."
                )
        );

        MvcResult result = mockMvc.perform(
                        get("/api/v1/admin/sentence-game/notes")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("미니게임 등록용 학습노트 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<Map<String, Object>> list = JsonPath.read(json, "$.data");

        assertThat(list.size()).isEqualTo(2);

        Map<String, Object> resp1 = list.stream()
                .filter(item -> ((Integer) item.get("id")).longValue() == note1.getId())
                .findFirst()
                .orElseThrow(() -> new AssertionError("note1 응답 없음"));

        assertThat(resp1.get("originalContent")).isEqualTo(note1.getOriginalContent());
        assertThat(resp1.get("correctedContent")).isEqualTo(note1.getCorrectedContent());

        Map<String, Object> resp2 = list.stream()
                .filter(item -> ((Integer) item.get("id")).longValue() == note2.getId())
                .findFirst()
                .orElseThrow(() -> new AssertionError("note2 응답 없음"));

        assertThat(resp2.get("originalContent")).isEqualTo(note2.getOriginalContent());
        assertThat(resp2.get("correctedContent")).isEqualTo(note2.getCorrectedContent());
    }

    @Test
    @DisplayName("미니게임 목록 조회 실패 - USER는 접근 불가")
    @WithUserDetails(value = "testMember", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getMiniGameList_forbidden() throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/sentence-game/notes")
                )
                .andExpect(status().isForbidden());
    }
}