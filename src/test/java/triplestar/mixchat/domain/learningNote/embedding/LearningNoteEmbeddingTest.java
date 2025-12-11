package triplestar.mixchat.domain.learningNote.embedding;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteDocumentRepository;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteEmbeddingService;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DisplayName("학습노트 임베딩 ES 저장 통합 테스트")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LearningNoteEmbeddingTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LearningNoteEmbeddingService embeddingService;

    @Autowired
    private LearningNoteDocumentRepository documentRepository;

    @Autowired
    @Qualifier("openAiEmbeddingModel")
    private EmbeddingModel embeddingModel;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LearningNoteRepository learningNoteRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(
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
    @DisplayName("학습노트 저장 후 임베딩 생성 -> ES에 문서가 저장된다")
    void embedding_is_saved_to_elasticsearch() {
        // given
        LearningNote note = LearningNote.create(
                testMember,
                "I goes to 학교.",
                "I go to school."
        );

        Feedback fb1 = Feedback.create(
                note,
                TranslationTagCode.GRAMMAR,
                "goes",
                "go",
                "삼인칭 단수 수정"
        );
        note.addFeedback(fb1);

        // JPA로 먼저 저장해서 ID 발급
        note = learningNoteRepository.save(note);

        // when: 임베딩 생성 + ES 저장
        embeddingService.index(note);

        // then: ES에 문서가 실제로 저장됐는지 확인
        var savedDoc = documentRepository.findById(note.getId());

        assertThat(savedDoc).isPresent();
        assertThat(savedDoc.get().getEmbedding()).isNotNull();

        float[] vector = savedDoc.get().getEmbedding();
        assertThat(vector.length).isGreaterThan(10);

        System.out.println("임베딩 벡터 길이 = " + vector.length);
        System.out.print("임베딩 벡터 + = ");
        for(int i=0; i<vector.length; i++) {
            System.out.print(" " + vector[i]);
        }
    }
}
