package triplestar.mixchat.domain.learningNote.learningNote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.transaction.Transactional;
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
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;
import triplestar.mixchat.testutils.TestMemberFactory;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("학습노트 생성 API 테스트")
class ApiV1LearningNoteControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private LearningNoteRepository learningNoteRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(TestMemberFactory.createMember("testUser1"));

        // I goes to 학교 every day. 입력
        LearningNote multiFeedbackNote = LearningNote.create(
                testMember,
                "I goes to 학교 every day.",
                "I go to school every day."
        );
        // 문법 피드백
        Feedback grammarFb = Feedback.create(
                multiFeedbackNote,
                TranslationTagCode.GRAMMAR,
                "goes",
                "go",
                "삼인칭 단수 동사 수정"
        );
        grammarFb.mark(); // 학습 완료

        // 번역 피드백
        Feedback translationFb = Feedback.create(
                multiFeedbackNote,
                TranslationTagCode.TRANSLATION,
                "학교",
                "school",
                "명사 번역"
        );

        // 단어 선택 피드백
        Feedback wordChoiceFb = Feedback.create(
                multiFeedbackNote,
                TranslationTagCode.VOCABULARY,
                "everyday",
                "every day",
                "자연스러운 표현으로 수정"
        );
        wordChoiceFb.mark(); // 학습 완료

        multiFeedbackNote.addFeedback(grammarFb);
        multiFeedbackNote.addFeedback(translationFb);
        multiFeedbackNote.addFeedback(wordChoiceFb);

        learningNoteRepository.save(multiFeedbackNote);

        // He like apple. 입력
        LearningNote singleTagNote = LearningNote.create(
                testMember,
                "He like apple.",
                "He likes apples."
        );
        Feedback grammarFb2 = Feedback.create(
                singleTagNote,
                TranslationTagCode.GRAMMAR,
                "like",
                "likes",
                "단수형 동사 수정"
        );
        singleTagNote.addFeedback(grammarFb2);
        learningNoteRepository.save(singleTagNote);
    }

    @Test
    @DisplayName("학습노트 생성 성공 - 피드백 포함 저장 검증")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createLearningNote_success() throws Exception {
        String requestJson = """
            {
              "memberId": %d,
              "originalContent": "I goes to 학교 every day.",
              "correctedContent": "I go to school every day.",
              "feedback": [
                {"tag": "GRAMMAR", "problem": "goes", "correction": "go", "extra": "시제 수정"},
                {"tag": "TRANSLATION", "problem": "학교", "correction": "school", "extra": "단어 번역"}
              ]
            }
            """.formatted(testMember.getId());

        mockMvc.perform(
                        post("/api/v1/learning/notes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("학습노트가 저장되었습니다."))
                .andExpect(jsonPath("$.data").isNumber());

        // DB 검증: 실제로 저장된 학습노트와 피드백 존재 확인
        var savedNote = learningNoteRepository.findAll().get(0);
        assertThat(savedNote.getOriginalContent()).isEqualTo("I goes to 학교 every day.");
        assertThat(savedNote.getCorrectedContent()).isEqualTo("I go to school every day.");

        var feedbacks = savedNote.getFeedbacks();
        assertThat(feedbacks).hasSize(2);

        assertThat(feedbacks).anySatisfy(f -> {
            assertThat(f.getTag().name()).isEqualTo("GRAMMAR");
            assertThat(f.getProblem()).isEqualTo("goes");
            assertThat(f.getCorrection()).isEqualTo("go");
            assertThat(f.getExtra()).isEqualTo("시제 수정");
        });

        assertThat(feedbacks).anySatisfy(f -> {
            assertThat(f.getTag().name()).isEqualTo("TRANSLATION");
            assertThat(f.getProblem()).isEqualTo("학교");
            assertThat(f.getCorrection()).isEqualTo("school");
            assertThat(f.getExtra()).isEqualTo("단어 번역");
        });
    }

    @Test
    @DisplayName("학습노트 생성 실패 - 필수값 누락 시 400 반환")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createLearningNote_missingField_fail() throws Exception {
        String invalidJson = """
            {
              "memberId": %d,
              "correctedContent": "I go to school every day.",
              "feedback": [
                {"tag": "GRAMMAR", "problem": "goes", "correction": "go", "extra": "시제 수정"}
              ]
            }
            """.formatted(testMember.getId());

        mockMvc.perform(post("/api/v1/learning/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("요청 값이 유효하지 않습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("학습노트 생성 실패 - 인증되지 않은 사용자 401 반환")
    void createLearningNote_unauthenticated_fail() throws Exception {
        String requestJson = """
            {
              "memberId": 999,
              "originalContent": "I goes to 학교 every day.",
              "correctedContent": "I go to school every day.",
              "feedback": [
                {"tag": "GRAMMAR", "problem": "goes", "correction": "go", "extra": "시제 수정"}
              ]
            }
            """;

        mockMvc.perform(post("/api/v1/learning/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("학습노트 목록 조회 성공 - GRAMMAR 태그 + LEARNED 상태 필터")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getLearningNotes_grammar_learned_success() throws Exception {
        mockMvc.perform(get("/api/v1/learning/notes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("tag", "GRAMMAR")
                        .param("status", "LEARNED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("학습노트 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].feedback[0].tag").value("GRAMMAR"))
                .andExpect(jsonPath("$.data[0].feedback[0].problem").value("goes"))
                .andExpect(jsonPath("$.data[0].feedback[0].correction").value("go"))
                .andExpect(jsonPath("$.data[0].feedback[0].extra").value("삼인칭 단수 수정"))
                .andExpect(jsonPath("$.data[0].feedback[0].marked").value(true));
    }

    @Test
    @DisplayName("학습노트 목록 조회 성공 - TRANSLATION 태그 + UNLEARNED 상태 필터")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getLearningNotes_translation_unlearned_success() throws Exception {
        mockMvc.perform(get("/api/v1/learning/notes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("tag", "TRANSLATION")
                        .param("status", "UNLEARNED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("학습노트 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].feedback[0].tag").value("TRANSLATION"))
                .andExpect(jsonPath("$.data[0].feedback[0].problem").value("학교"))
                .andExpect(jsonPath("$.data[0].feedback[0].correction").value("school"))
                .andExpect(jsonPath("$.data[0].feedback[0].extra").value("단어 번역"))
                .andExpect(jsonPath("$.data[0].feedback[0].marked").value(false));
    }

    @Test
    @DisplayName("학습노트 목록 조회 성공 - VOCABULARY 태그 + ALL 상태 필터")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getLearningNotes_wordChoice_all_success() throws Exception {
        mockMvc.perform(get("/api/v1/learning/notes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("tag", "VOCABULARY")
                        .param("status", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("학습노트 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].feedback[0].tag").value("VOCABULARY"))
                .andExpect(jsonPath("$.data[0].feedback[0].problem").value("everyday"))
                .andExpect(jsonPath("$.data[0].feedback[0].correction").value("every day"))
                .andExpect(jsonPath("$.data[0].feedback[0].extra").value("표현 수정"))
                .andExpect(jsonPath("$.data[0].feedback[0].marked").value(true));
    }

    @Test
    @DisplayName("학습노트 목록 조회 실패 - 인증되지 않은 사용자 401 반환")
    void getLearningNotes_unauthenticated_fail() throws Exception {
        mockMvc.perform(get("/api/v1/learning/notes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("tag", "GRAMMAR")
                        .param("status", "LEARNED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService")
    @DisplayName("학습노트 목록 조회 실패 - 잘못된 파라미터 400 반환")
    void getLearningNotes_invalidParam_fail() throws Exception {
        mockMvc.perform(get("/api/v1/learning/notes")
                        .param("page", "-1")
                        .param("size", "-10")
                        .param("tag", "INVALID_TAG")
                        .param("status", "UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}