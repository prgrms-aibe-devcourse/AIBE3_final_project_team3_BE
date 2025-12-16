package triplestar.mixchat.domain.learningNote.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.learningNote.constant.TranslationTagCode;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@Entity
@Table(name = "feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_note_id", nullable = false)
    private LearningNote learningNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TranslationTagCode tag;

    @Column(nullable = false)
    private String problem;

    @Column(nullable = false)
    private String correction;

    @Column(nullable = false)
    private String extra;

    @Column(name = "is_marked", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean marked = false;

    private Feedback(LearningNote learningNote,
                       TranslationTagCode tag,
                       String problem,
                       String correction,
                       String extra) {
        if (learningNote == null) {
            throw new IllegalArgumentException("learningNote는 null일 수 없습니다.");
        }
        if (tag == null) {
            throw new IllegalArgumentException("tag는 null일 수 없습니다.");
        }
        if (problem == null || problem.isBlank()) {
            throw new IllegalArgumentException("problem은 비어 있을 수 없습니다.");
        }
        if (correction == null || correction.isBlank()) {
            throw new IllegalArgumentException("correction은 비어 있을 수 없습니다.");
        }
        if (extra == null || extra.isBlank()) {
            throw new IllegalArgumentException("extra은 비어 있을 수 없습니다.");
        }
        this.learningNote = learningNote;
        this.tag = tag;
        this.problem = problem;
        this.correction = correction;
        this.extra = extra;
    }

    public static Feedback create(LearningNote learningNote,
                                  TranslationTagCode tag,
                                  String problem,
                                  String correction,
                                  String extra) {
        return new Feedback(learningNote, tag, problem, correction, extra);
    }

    void modifyLearningNote(LearningNote learningNote) {
        this.learningNote = learningNote;
    }

    public void mark() {
        this.marked = true;
    }

    public void unmark() {
        this.marked = false;
    }
}