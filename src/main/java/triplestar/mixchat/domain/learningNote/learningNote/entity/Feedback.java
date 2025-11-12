package triplestar.mixchat.domain.learningNote.learningNote.entity;


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
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;
import triplestar.mixchat.global.jpa.entity.BaseEntityNoModified;

@Entity
@Table(name = "feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback extends BaseEntityNoModified {

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

    protected Feedback(LearningNote learningNote,
                       TranslationTagCode tag,
                       String problem,
                       String correction,
                       String extra) {
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
}