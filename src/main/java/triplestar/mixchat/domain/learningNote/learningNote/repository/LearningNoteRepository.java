package triplestar.mixchat.domain.learningNote.learningNote.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.learningNote.learningNote.constant.LearningStatus;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;

public interface LearningNoteRepository extends JpaRepository<LearningNote, Long>{

    @Query("""
        select distinct n from LearningNote n
        join fetch n.feedbacks f
        join fetch n.member m
        where m.id = :memberId
          and f.tag = :tag
         and (
                    :#{#status.name()} = 'ALL'
                 or (:#{#status.name()} = 'LEARNED' and f.marked = true)
                 or (:#{#status.name()} = 'UNLEARNED' and f.marked = false)
               )
        order by n.createdAt desc
    """)
    List<LearningNote> findByMemberWithFilters(
            @Param("memberId") Long memberId,
            @Param("tag") TranslationTagCode tag,
            @Param("status") LearningStatus status,
            Pageable pageable
    );
}