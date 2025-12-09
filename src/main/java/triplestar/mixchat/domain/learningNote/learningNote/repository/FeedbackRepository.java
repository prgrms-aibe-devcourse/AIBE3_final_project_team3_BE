package triplestar.mixchat.domain.learningNote.learningNote.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("""
        SELECT f
        FROM Feedback f
        JOIN FETCH f.learningNote n
        WHERE n.member.id = :memberId
        AND (:tag = 'ALL' OR f.tag = :tag)
        AND (:isMarked IS NULL OR f.marked = :isMarked)
    """)
    Page<Feedback> findFeedbacksByMember(
            Long memberId,
            TranslationTagCode tag,
            Boolean isMarked,
            Pageable pageable
    );
}