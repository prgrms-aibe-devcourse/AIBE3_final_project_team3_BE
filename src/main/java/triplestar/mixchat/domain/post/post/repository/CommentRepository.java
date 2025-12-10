package triplestar.mixchat.domain.post.post.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.post.post.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    @Query("""
            SELECT c FROM Comment c
            WHERE c.id = :commentId AND c.post.id = :postId
            """)
    Optional<Comment> findByIdAndPostId(Long commentId, Long postId);

    /**
     * 게시글의 댓글과 대댓글을 fetch join으로 한번에 조회 (N+1 문제 해결)
     */
    @Query("""
            SELECT DISTINCT c FROM Comment c
            LEFT JOIN FETCH c.author
            LEFT JOIN FETCH c.replies r
            LEFT JOIN FETCH r.author
            WHERE c.post.id = :postId AND c.parent IS NULL
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findByPostIdWithReplies(@Param("postId") Long postId);
}

