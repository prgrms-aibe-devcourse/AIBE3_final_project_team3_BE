package triplestar.mixchat.domain.post.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.post.post.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            SELECT DISTINCT p FROM Post p
            LEFT JOIN FETCH p.author
            LEFT JOIN FETCH p.images
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    Page<Post> findLatest(Pageable pageable);

    /**
     * 인기순으로 게시글을 조회합니다.
     * Native Query를 사용하여 좋아요 수 기준으로 정확한 정렬을 보장합니다.
     *
     * 정렬 우선순위:
     * 1. 좋아요 수 (내림차순)
     * 2. 생성일시 (내림차순)
     * 3. 게시글 ID (내림차순)
     */
    @Query(value = """
            SELECT DISTINCT p.* FROM posts p
            LEFT JOIN post_likes pl ON p.id = pl.post_id
            GROUP BY p.id
            ORDER BY COUNT(pl.member_id) DESC, p.created_at DESC, p.id DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p.id) FROM posts p
            """,
            nativeQuery = true)
    Page<Post> findPopular(Pageable pageable);

    /**
     * ID 리스트로 게시글을 조회합니다. (N+1 방지를 위한 EntityGraph 사용)
     * 인기게시글 조회 후 연관 엔티티를 로딩할 때 사용됩니다.
     */
    @EntityGraph(attributePaths = {"author", "images"})
    @Query("SELECT p FROM Post p WHERE p.id IN :ids")
    List<Post> findByIdsWithFetch(@Param("ids") List<Long> ids);
}

