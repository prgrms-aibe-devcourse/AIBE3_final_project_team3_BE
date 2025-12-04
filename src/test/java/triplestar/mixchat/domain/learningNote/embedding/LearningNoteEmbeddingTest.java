package triplestar.mixchat.domain.learningNote.embedding;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteDocumentRepository;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteEmbeddingService;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("학습노트 임베딩 저장 테스트")
class LearningNoteEmbeddingTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LearningNoteEmbeddingService embeddingService;

    @Autowired
    private LearningNoteDocumentRepository documentRepository;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(
                Member.createMember(
                        "target1@example.com",
                        Password.encrypt("Password1!", passwordEncoder),
                        "신고대상1",
                        "target1",
                        Country.KR,
                        EnglishLevel.BEGINNER,
                        List.of("travel"),
                        "신고 대상 유저1"
                )
        );
    }

    @Test
    @DisplayName("학습노트 저장 시 임베딩이 생성되고 ES에 저장된다")
    void embedding_is_saved_to_elasticsearch() {
        // given
        LearningNote note = LearningNote.create(
                testMember,
                "I goes to 학교.",
                "I go to school."
        );

        Feedback fb1 = Feedback.create(note, TranslationTagCode.GRAMMAR, "goes", "go", "삼인칭 단수 수정");
        note.addFeedback(fb1);

        // 임시 ID 부여 (보통 JPA 저장 후 ID가 생성되지만, 여기서는 단독 테스트)


        // when: 임베딩 생성 + ES 저장
        embeddingService.index(note);

        // then: ES에 문서가 실제로 저장됐는지 확인
        var savedDoc = documentRepository.findById(note.getId());

        assertThat(savedDoc).isPresent();
        assertThat(savedDoc.get().getEmbedding()).isNotNull();

        float[] vector = savedDoc.get().getEmbedding();
        assertThat(vector.length).isGreaterThan(10); // 임베딩 길이 검증

        System.out.println("임베딩 벡터 길이 = " + vector.length);
    }
}