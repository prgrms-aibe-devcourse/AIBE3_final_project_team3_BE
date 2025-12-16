package triplestar.mixchat.domain.post.post.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.post.post.entity.Comment;
import triplestar.mixchat.domain.post.post.entity.CommentLike;
import triplestar.mixchat.domain.post.post.entity.CommentLikeId;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {

    Optional<CommentLike> findByMemberAndComment(Member member, Comment comment);

    boolean existsByMemberAndComment(Member member, Comment comment);

    void deleteByMemberAndComment(Member member, Comment comment);

    /**
     * 댓글의 실제 좋아요 개수를 조회합니다.
     * likeCount 필드 대신 실제 데이터를 조회하여 정합성을 보장합니다.
     */
    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment = :comment")
    int countByComment(@Param("comment") Comment comment);

    /**
     * 댓글 ID로 좋아요 개수를 조회합니다.
     */
    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment.id = :commentId")
    int countByCommentId(@Param("commentId") Long commentId);

    /**
     * 여러 댓글의 좋아요 개수를 한번에 조회합니다. (Batch 조회로 성능 최적화)
     */
    @Query("""
            SELECT cl.comment.id as commentId, COUNT(cl) as likeCount
            FROM CommentLike cl
            WHERE cl.comment.id IN :commentIds
            GROUP BY cl.comment.id
            """)
    List<CommentLikeCount> countByCommentIds(@Param("commentIds") List<Long> commentIds);

    /**
     * 댓글 좋아요 수 조회 결과 DTO
     */
    interface CommentLikeCount {
        Long getCommentId();
        Long getLikeCount();
    }
}

