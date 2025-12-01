package triplestar.mixchat.domain.learningNote.learningNote.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;

public interface LearningNoteRepository extends JpaRepository<LearningNote, Long>{

    @Query("""
                    SELECT ln FROM LearningNote ln
                    WHERE ln.member.id = :memberId
                    ORDER BY ln.createdAt DESC
                    LIMIT :maxItems
    """)
    List<LearningNote> findTopNByMemberId(Long memberId, int maxItems);
}