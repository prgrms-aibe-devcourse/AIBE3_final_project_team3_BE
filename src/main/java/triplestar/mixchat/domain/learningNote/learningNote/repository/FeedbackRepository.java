package triplestar.mixchat.domain.learningNote.learningNote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}