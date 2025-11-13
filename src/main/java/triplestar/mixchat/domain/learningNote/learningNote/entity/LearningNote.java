package triplestar.mixchat.domain.learningNote.learningNote.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.jpa.entity.BaseEntityNoModified;

@Entity
@Table(name = "learning_notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningNote extends BaseEntityNoModified {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String originalContent;

    @Column(nullable = false)
    private String correctedContent;

    @Column(name = "is_marked", nullable = false)
    private boolean marked = false;

    @OneToMany(mappedBy = "learningNote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();

    private LearningNote(Member member,
                           String originalContent,
                           String correctedContent,
                           boolean marked) {
        if (member == null) {
            throw new IllegalArgumentException("member는 null일 수 없습니다.");
        }
        if (originalContent == null || originalContent.isBlank()) {
            throw new IllegalArgumentException("originalContent는 비어 있을 수 없습니다.");
        }
        if (correctedContent == null || correctedContent.isBlank()) {
            throw new IllegalArgumentException("correctedContent는 비어 있을 수 없습니다.");
        }
        this.member = member;
        this.originalContent = originalContent;
        this.correctedContent = correctedContent;
        this.marked = marked;
    }

    public static LearningNote create(Member member,
                                      String originalContent,
                                      String correctedContent) {
        return new LearningNote(member, originalContent, correctedContent, false);
    }

    // 연관관계 편의 메서드
    public void addFeedback(Feedback feedback) {
        feedbacks.add(feedback);
        feedback.modifyLearningNote(this);
    }

    public void mark() {
        this.marked = true;
    }

    public void unmark() {
        this.marked = false;
    }
}