package triplestar.mixchat.domain.learningNote.learningNote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import triplestar.mixchat.domain.learningNote.learningNote.constant.LearningFilter;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.FeedbackRepository;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;
import triplestar.mixchat.testutils.TestMemberFactory;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("학습노트 API 테스트")
class ApiV1LearningNoteControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private LearningNoteRepository learningNoteRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;

    private Member testMember;
    private Member anotherMember;
    private Long fb1Id;
    private Long fb2Id;
    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(TestMemberFactory.createMember("testUser1"));

        anotherMember = memberRepository.save(TestMemberFactory.createMember("anotherUser"));

        // 학습노트 #1 (GRAMMAR + TRANSLATION 혼합)
        LearningNote note1 = LearningNote.create(testMember, "I goes to 학교.", "I go to school.");
        Feedback fb1 = Feedback.create(note1, TranslationTagCode.GRAMMAR, "goes", "go", "삼인칭 단수 수정");
        Feedback fb2 = Feedback.create(note1, TranslationTagCode.TRANSLATION, "학교", "school", "단어 번역");
        note1.addFeedback(fb1);
        note1.addFeedback(fb2);

        // fb1은 학습 완료 (LEARNED)
        fb1.mark();

        // 학습노트 #2 (GRAMMAR 중복 태그)
        LearningNote note2 = LearningNote.create(testMember, "He walk every day.", "He walks every days.");
        Feedback fb3 = Feedback.create(note2, TranslationTagCode.GRAMMAR, "walk", "walks", "3인칭 단수");
        Feedback fb4 = Feedback.create(note2, TranslationTagCode.GRAMMAR, "day", "days", "복수형 수정");
        note2.addFeedback(fb3);
        note2.addFeedback(fb4);

        // fb3은 학습 완료, fb4는 미완료
        fb3.mark();

        // 학습노트 #3 (TRANSLATION 전용)
        LearningNote note3 = LearningNote.create(testMember, "나는 사과를 먹는다.", "I eat an apple.");
        Feedback fb5 = Feedback.create(note3, TranslationTagCode.TRANSLATION, "사과를", "an apple", "직역 수정");
        Feedback fb6 = Feedback.create(note3, TranslationTagCode.TRANSLATION, "먹는다", "eat", "직역 수정");
        note3.addFeedback(fb5);
        note3.addFeedback(fb6);
        learningNoteRepository.saveAll(List.of(note1, note2, note3));
        fb1Id = fb1.getId();
        fb2Id = fb2.getId();
    }

    @Test
    @DisplayName("학습노트 생성 성공 - 피드백 포함 저장 검증")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createLearningNote_success() throws Exception {
        String requestJson = """
            {
              "originalContent": "I goes to 학교 every day.",
              "correctedContent": "I go to school every day.",
              "feedback": [
                {"tag": "GRAMMAR", "problem": "goes", "correction": "go", "extra": "시제 수정"},
                {"tag": "TRANSLATION", "problem": "학교", "correction": "school", "extra": "단어 번역"}
              ]
            }
            """;

        mockMvc.perform(
                        post("/api/v1/learning-notes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("학습노트가 저장되었습니다."))
                .andExpect(jsonPath("$.data").isNumber());

        // DB 검증: 실제로 저장된 학습노트와 피드백 존재 확인
        var savedNote = learningNoteRepository.findAll().get(3);
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
              "correctedContent": "I go to school every day.",
              "feedback": [
                {"tag": "GRAMMAR", "problem": "goes", "correction": "go", "extra": "시제 수정"}
              ]
            }
            """;

        mockMvc.perform(post("/api/v1/learning-notes")
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
              "originalContent": "I goes to 학교 every day.",
              "correctedContent": "I go to school every day.",
              "feedback": [
                {"tag": "GRAMMAR", "problem": "goes", "correction": "go", "extra": "시제 수정"}
              ]
            }
            """;

        mockMvc.perform(post("/api/v1/learning-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("학습노트 목록 조회 성공 - GRAMMAR 태그 + LEARNED 상태 (2개 결과)")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getLearningNotes_grammar_learned_success() throws Exception {
        mockMvc.perform(get("/api/v1/learning-notes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("tag", TranslationTagCode.GRAMMAR.toString())
                        .param("learningFilter", LearningFilter.LEARNED.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("학습노트 목록 조회 성공"))

                // 결과 개수 = 2
                .andExpect(jsonPath("$.data.content.length()").value(2))

                // content[0]
                .andExpect(jsonPath("$.data.content[0].feedback.tag").value("GRAMMAR"))
                .andExpect(jsonPath("$.data.content[0].feedback.problem").value("goes"))
                .andExpect(jsonPath("$.data.content[0].feedback.correction").value("go"))
                .andExpect(jsonPath("$.data.content[0].feedback.extra").value("삼인칭 단수 수정"))

                // content[1]
                .andExpect(jsonPath("$.data.content[1].feedback.tag").value("GRAMMAR"))
                .andExpect(jsonPath("$.data.content[1].feedback.problem").value("walk"))
                .andExpect(jsonPath("$.data.content[1].feedback.correction").value("walks"))
                .andExpect(jsonPath("$.data.content[1].feedback.extra").value("3인칭 단수"));
    }

    @Test
    @DisplayName("학습노트 목록 조회 성공 - TRANSLATION 태그 + UNLEARNED 상태 (3개 결과)")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getLearningNotes_translation_unlearned_success() throws Exception {

        mockMvc.perform(get("/api/v1/learning-notes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("tag", TranslationTagCode.TRANSLATION.toString())
                        .param("learningFilter", LearningFilter.UNLEARNED.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("학습노트 목록 조회 성공"))

                // 총 3개
                .andExpect(jsonPath("$.data.content.length()").value(3))

                // content[0]
                .andExpect(jsonPath("$.data.content[0].feedback.tag").value("TRANSLATION"))
                .andExpect(jsonPath("$.data.content[0].feedback.problem").value("학교"))
                .andExpect(jsonPath("$.data.content[0].feedback.correction").value("school"))
                .andExpect(jsonPath("$.data.content[0].feedback.extra").value("단어 번역"))

                // content[1]
                .andExpect(jsonPath("$.data.content[1].feedback.tag").value("TRANSLATION"))
                .andExpect(jsonPath("$.data.content[1].feedback.problem").value("사과를"))
                .andExpect(jsonPath("$.data.content[1].feedback.correction").value("an apple"))
                .andExpect(jsonPath("$.data.content[1].feedback.extra").value("직역 수정"))

                // content[2]
                .andExpect(jsonPath("$.data.content[2].feedback.tag").value("TRANSLATION"))
                .andExpect(jsonPath("$.data.content[2].feedback.problem").value("먹는다"))
                .andExpect(jsonPath("$.data.content[2].feedback.correction").value("eat"))
                .andExpect(jsonPath("$.data.content[2].feedback.extra").value("직역 수정"));
    }

    @Test
    @DisplayName("학습노트 목록 조회 성공 - GRAMMAR 태그 + ALL 상태 (3개 결과)")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getLearningNotes_grammar_all_success() throws Exception {

        mockMvc.perform(get("/api/v1/learning-notes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("tag", TranslationTagCode.GRAMMAR.toString())
                        .param("learningFilter", LearningFilter.ALL.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("학습노트 목록 조회 성공"))

                // 총 3개 (fb1, fb3, fb4)
                .andExpect(jsonPath("$.data.content.length()").value(3))

                // content[0]
                .andExpect(jsonPath("$.data.content[0].feedback.tag").value("GRAMMAR"))
                .andExpect(jsonPath("$.data.content[0].feedback.problem").value("goes"))
                .andExpect(jsonPath("$.data.content[0].feedback.correction").value("go"))
                .andExpect(jsonPath("$.data.content[0].feedback.extra").value("삼인칭 단수 수정"))

                // content[1]
                .andExpect(jsonPath("$.data.content[1].feedback.tag").value("GRAMMAR"))
                .andExpect(jsonPath("$.data.content[1].feedback.problem").value("walk"))
                .andExpect(jsonPath("$.data.content[1].feedback.correction").value("walks"))
                .andExpect(jsonPath("$.data.content[1].feedback.extra").value("3인칭 단수"))

                // content[2]
                .andExpect(jsonPath("$.data.content[2].feedback.tag").value("GRAMMAR"))
                .andExpect(jsonPath("$.data.content[2].feedback.problem").value("day"))
                .andExpect(jsonPath("$.data.content[2].feedback.correction").value("days"))
                .andExpect(jsonPath("$.data.content[2].feedback.extra").value("복수형 수정"));
    }
    @Test
    @DisplayName("학습노트 목록 조회 실패 - 잘못된 태그 입력 시 400 반환")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getLearningNotes_invalidTag_fail_noBody() throws Exception {
        mockMvc.perform(get("/api/v1/learning-notes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("tag", "WRONG_TAG") // 존재하지 않는 Enum
                        .param("filter", "ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("학습노트 목록 조회 실패 - 필수 파라미터 누락 시 400 반환")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getLearningNotes_missingParams_fail() throws Exception {
        mockMvc.perform(get("/api/v1/learning-notes")
                        .param("page", "0")
                        .param("size", "10")
                        // tag와 filter 생략
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공 - 피드백 상태를 학습 완료로 변경한다 (marked=true)")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateFeedbackMark_success_toLearned() throws Exception {
        String requestJson = """
                { "marked": true }
                """;

        mockMvc.perform(patch("/api/v1/learning-notes/feedbacks/{feedbackId}/mark/learned", fb2Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("피드백이 학습 완료로 변경되었습니다."));

        Feedback updated = feedbackRepository.findById(fb2Id).orElseThrow();
        assertThat(updated.isMarked()).isTrue();
    }

    @Test
    @DisplayName("성공 - 피드백 상태를 학습 미완료로 변경한다 (marked=false)")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateFeedbackMark_success_toUnlearned() throws Exception {
        String requestJson = """
                { "marked": false }
                """;

        mockMvc.perform(patch("/api/v1/learning-notes/feedbacks/{feedbackId}/mark/unlearned", fb1Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("피드백이 학습 미완료로 변경되었습니다."));

        Feedback updated = feedbackRepository.findById(fb1Id).orElseThrow();
        assertThat(updated.isMarked()).isFalse();
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 피드백 ID로 요청 시 404 반환")
    @WithUserDetails(value = "testUser1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateFeedbackMark_fail_notFound() throws Exception {
        String requestJson = """
                { "marked": true }
                """;

        mockMvc.perform(patch("/api/v1/learning-notes/feedbacks/{feedbackId}/mark/learned", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("존재하지 않는 엔티티에 접근했습니다."));
    }
}