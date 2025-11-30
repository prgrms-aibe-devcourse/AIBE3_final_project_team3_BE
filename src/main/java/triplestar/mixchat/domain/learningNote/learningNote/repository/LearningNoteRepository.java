package triplestar.mixchat.domain.learningNote.learningNote.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;

public interface LearningNoteRepository extends JpaRepository<LearningNote, Long>{
    List<LearningNote> findTopNByUserIdOrderByCreatedAtDesc(Long userId, int maxItems);
}