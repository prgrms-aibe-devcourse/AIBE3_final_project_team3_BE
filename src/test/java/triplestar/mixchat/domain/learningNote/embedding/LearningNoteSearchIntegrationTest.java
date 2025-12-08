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
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteSaveService;
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
    private LearningNoteSaveService searchService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member userA;
    private Member userB;

    @BeforeEach
    void setup() {
        userA = memberRepository.save(
                Member.createMember(
                        "userA@example.com",
                        Password.encrypt("Password1!", passwordEncoder),
                        "UserA",
                        "userA",
                        Country.KR,
                        EnglishLevel.INTERMEDIATE,
                        List.of("study"),
                        "검색 테스트 A"
                )
        );

        userB = memberRepository.save(
                Member.createMember(
                        "userB@example.com",
                        Password.encrypt("Password1!", passwordEncoder),
                        "UserB",
                        "userB",
                        Country.US,
                        EnglishLevel.INTERMEDIATE,
                        List.of("travel"),
                        "검색 테스트 B"
                )
        );
    }

    @Test
    @DisplayName("최근 학습노트를 기반으로 타 유저의 유사한 학습노트를 검색한다")
    void search_by_recent_notes_with_other_user_data() {

        LearningNote a1 = LearningNote.create(
                userA,
                "I goes to school yesterday.",
                "I went to school yesterday."
        );
        a1.addFeedback(Feedback.create(a1, TranslationTagCode.GRAMMAR, "goes", "went", "과거형 수정"));
        a1 = learningNoteRepository.save(a1);
        embeddingService.index(a1);

        LearningNote a2 = LearningNote.create(
                userA,
                "She don't likes apple.",
                "She doesn't like apples."
        );
        a2.addFeedback(Feedback.create(a2, TranslationTagCode.GRAMMAR, "don't", "doesn't", "삼인칭 단수 수정"));
        a2 = learningNoteRepository.save(a2);
        embeddingService.index(a2);

        LearningNote b1 = LearningNote.create(
                userB,
                "I went to school last night.",
                "I went to school last night."
        );
        b1.addFeedback(Feedback.create(b1, TranslationTagCode.GRAMMAR, "went", "went", "문법 동일"));
        b1 = learningNoteRepository.save(b1);
        embeddingService.index(b1);

        LearningNote b2 = LearningNote.create(
                userB,
                "She doesn't like banana either.",
                "She doesn't like bananas either."
        );
        b2.addFeedback(Feedback.create(b2, TranslationTagCode.GRAMMAR, "doesn't", "doesn't", "유사 문장"));
        b2 = learningNoteRepository.save(b2);
        embeddingService.index(b2);

        searchService.saveByRecentNotes(1L, userA.getId());

        List<LearningNote> results = searchService.loadNotesFromCache(1L, userA.getId());

        List<Long> resultIds = results.stream()
            .map(LearningNote::getId)
            .toList();

        Long a1Id = a1.getId();
        Long a2Id = a2.getId();
        Long b1Id = b1.getId();
        Long b2Id = b2.getId();

        assertThat(resultIds.contains(a1Id)).isFalse();
        assertThat(resultIds.contains(a2Id)).isFalse();

        assertThat(resultIds.contains(b1Id)).isTrue();
        assertThat(resultIds.contains(b2Id)).isTrue();

//        System.out.println("\n=== 검색 결과 ===");
//        for (LearningNote n : results) {
//            System.out.println("NoteId=" + n.getId() + " / Original=" + n.getOriginalContent());
//        }
    }
}

