package triplestar.mixchat.domain.learningNote.learningNote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;

public interface LearningNoteRepository extends JpaRepository<LearningNote, Long>{
}