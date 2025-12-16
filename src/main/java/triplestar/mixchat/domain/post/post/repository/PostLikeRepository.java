package triplestar.mixchat.domain.post.post.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.post.post.entity.Post;
import triplestar.mixchat.domain.post.post.entity.PostLike;
import triplestar.mixchat.domain.post.post.entity.PostLikeId;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    Optional<PostLike> findByMemberAndPost(Member member, Post post);

    boolean existsByMemberAndPost(Member member, Post post);

    void deleteByMemberAndPost(Member member, Post post);

    /**
     * 게시글의 실제 좋아요 개수를 조회합니다.
     * likeCount 필드 대신 실제 데이터를 조회하여 정합성을 보장합니다.
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = :post")
    int countByPost(@Param("post") Post post);

    /**
     * 게시글 ID로 좋아요 개수를 조회합니다.
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId")
    int countByPostId(@Param("postId") Long postId);

    /**
     * 여러 게시글의 좋아요 개수를 한번에 조회합니다. (Batch 조회로 성능 최적화)
     */
    @Query("""
            SELECT pl.post.id as postId, COUNT(pl) as likeCount
            FROM PostLike pl
            WHERE pl.post.id IN :postIds
            GROUP BY pl.post.id
            """)
    List<PostLikeCount> countByPostIds(@Param("postIds") List<Long> postIds);

    /**
     * 특정 사용자가 여러 게시글에 좋아요를 눌렀는지 확인합니다. (Batch 조회로 성능 최적화)
     */
    @Query("""
            SELECT pl.post.id
            FROM PostLike pl
            WHERE pl.member.id = :memberId AND pl.post.id IN :postIds
            """)
    List<Long> findLikedPostIdsByMemberIdAndPostIds(@Param("memberId") Long memberId, @Param("postIds") List<Long> postIds);

    /**
     * 게시글 좋아요 수 조회 결과 DTO
     */
    interface PostLikeCount {
        Long getPostId();
        Long getLikeCount();
    }
}

