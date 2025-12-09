package triplestar.mixchat.domain.admin.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired LearningNoteRepository learningNoteRepository;
    @Autowired ObjectMapper objectMapper;
    @Autowired SentenceGameRepository sentenceGameRepository;

    private Member testAdmin;
    private Member testMember;

    @BeforeEach
    void setUp() {
        testAdmin = memberRepository.save(TestMemberFactory.createAdmin("testAdmin"));
        testMember = memberRepository.save(TestMemberFactory.createMember("testMember"));
    }

    // -------------------------------------------------------
    @Test
    @DisplayName("관리자 미니게임 문장 등록 성공")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createMiniGame_success() throws Exception {

        // ⭐ 학습노트 먼저 DB에 저장해야함
        LearningNote note = learningNoteRepository.save(
                LearningNote.create(testMember, "I goed to school.", "I went to school.")
        );

        AdminSentenceGameCreateReq req = new AdminSentenceGameCreateReq(note.getId());

        MvcResult result = mockMvc.perform(
                        post("/api/v1/admin/sentence-game")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("미니게임 문장이 등록되었습니다."))
                .andExpect(jsonPath("$.data.sentenceGameId").isNumber())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Integer idInt = JsonPath.read(responseBody, "$.data.sentenceGameId");
        Long generatedId = idInt.longValue();

        SentenceGame saved = sentenceGameRepository.findById(generatedId)
                .orElseThrow();

        assertThat(saved.getOriginalContent()).isEqualTo("I goed to school.");
        assertThat(saved.getCorrectedContent()).isEqualTo("I went to school.");
    }

    @Test
    @DisplayName("미니게임 등록 실패 - 존재하지 않는 학습노트 ID")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createMiniGame_validation_fail() throws Exception {

        AdminSentenceGameCreateReq req = new AdminSentenceGameCreateReq(99999L);

        mockMvc.perform(
                        post("/api/v1/admin/sentence-game")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("미니게임 등록 실패 - 관리자 권한이 아니면 403")
    @WithUserDetails(value = "testMember", userDetailsServiceBeanName = "testUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createMiniGame_forbidden() throws Exception {

        AdminSentenceGameCreateReq req = new AdminSentenceGameCreateReq(1L);

        mockMvc.perform(
                        post("/api/v1/admin/sentence-game")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("미니게임 등록용 전체 학습노트 목록 조회 성공")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getSentenceGameNoteList_success() throws Exception {

        LearningNote note1 = learningNoteRepository.save(
                LearningNote.create(testMember, "I goed to school.", "I went to school.")
        );

        LearningNote note2 = learningNoteRepository.save(
                LearningNote.create(testMember, "She don't like apples.", "She doesn't like apples.")
        );

        MvcResult result = mockMvc.perform(
                        get("/api/v1/admin/sentence-game/notes")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("미니게임 등록용 학습노트 목록 조회 성공"))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<Map<String, Object>> list = JsonPath.read(json, "$.data.content");

        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("관리자 미니게임 문장 목록 조회 성공")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getMiniGameList_success() throws Exception {

        sentenceGameRepository.save(
                SentenceGame.createSentenceGame("I goed", "I went", List.of())
        );
        sentenceGameRepository.save(
                SentenceGame.createSentenceGame("She dont like apple", "She does not like apples", List.of())
        );

        MvcResult result = mockMvc.perform(
                        get("/api/v1/admin/sentence-game")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("문장게임 목록 조회 성공"))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<Map<String, Object>> list = JsonPath.read(json, "$.data.content");

        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("문장게임 삭제 성공")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteMiniGame_success() throws Exception {

        SentenceGame saved = sentenceGameRepository.save(
                SentenceGame.createSentenceGame("I goed", "I went", List.of())
        );

        Long id = saved.getId();

        mockMvc.perform(
                        delete("/api/v1/admin/sentence-game/" + id)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("문장게임 문장이 삭제되었습니다."));

        assertThat(sentenceGameRepository.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("문장게임 삭제 실패 - 존재하지 않는 ID")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteMiniGame_notFound() throws Exception {

        mockMvc.perform(
                        delete("/api/v1/admin/sentence-game/99999")
                )
                .andExpect(status().isBadRequest());
    }
}
