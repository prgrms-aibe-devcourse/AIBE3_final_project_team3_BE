package triplestar.mixchat.domain.learningNote.embedding;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteEmbeddingService;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteSearchService;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("학습노트 임베딩 저장 + 검색(KNN) 통합 테스트")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LearningNoteSearchIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LearningNoteRepository learningNoteRepository;

    @Autowired
    private LearningNoteEmbeddingService embeddingService;

    @Autowired
    private LearningNoteSearchService searchService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member member;

    @BeforeEach
    void setup() {
        member = memberRepository.save(
                Member.createMember(
                        "search_test@example.com",
                        Password.encrypt("Password1!", passwordEncoder),
                        "테스터",
                        "tester",
                        Country.KR,
                        EnglishLevel.INTERMEDIATE,
                        List.of("study"),
                        "검색 테스트"
                )
        );
    }

    @Test
    @DisplayName("ES KNN 검색이 유사한 학습노트를 반환해야 한다")
    void search_knn_success() {

        // ⭐ 1) 저장할 학습노트 1
        LearningNote note1 = LearningNote.create(
                member,
                "I goes to school yesterday.",
                "I went to school yesterday."
        );
        Feedback fb1 = Feedback.create(note1, TranslationTagCode.GRAMMAR, "goes", "went", "과거형으로 수정");
        note1.addFeedback(fb1);
        note1 = learningNoteRepository.save(note1);

        // ⭐ 2) 저장할 학습노트 2
        LearningNote note2 = LearningNote.create(
                member,
                "She don't likes apple.",
                "She doesn't like apples."
        );
        Feedback fb2 = Feedback.create(note2, TranslationTagCode.GRAMMAR, "don't", "doesn't", "삼인칭 단수 수정");
        note2.addFeedback(fb2);
        note2 = learningNoteRepository.save(note2);

        // ⭐ 3) ES에 임베딩 저장
        embeddingService.index(note1);
        embeddingService.index(note2);

        // ⭐ 4) 검색 수행 (쿼리 문장과 가장 비슷한 노트를 찾아야 함)
        String query = "I went to school yesterday";

        List<LearningNote> results = searchService.searchRelevantNotes(member.getId(), query);

        // ⭐ 5) 검증
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getId()).isEqualTo(note1.getId()); // note1이 가장 유사해야 함

        System.out.println("\n=== 검색 결과 ===");
        results.forEach(n ->
                System.out.println("• Note ID: " + n.getId() + " / Original: " + n.getOriginalContent())
        );
    }
}

