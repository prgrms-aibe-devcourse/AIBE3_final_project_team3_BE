package triplestar.mixchat.domain.learningNote.learningNote.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("""
        select f from Feedback f
        join fetch f.learningNote n
        join fetch n.member m
        where f.id = :feedbackId and m.id = :memberId
    """)
    Optional<Feedback> findByIdAndMemberId(
            @Param("feedbackId") Long feedbackId,
            @Param("memberId") Long memberId
    );
}