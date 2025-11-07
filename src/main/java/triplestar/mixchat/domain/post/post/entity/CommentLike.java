package triplestar.mixchat.domain.post.post.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.EmbeddedId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;

/**
 * 댓글 좋아요 엔티티 (복합키: member_id + comment_id)
 */
@Getter
@Entity
@Table(
    name = "comment_likes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_comment_like_member_comment", columnNames = {"member_id", "comment_id"})
    },
    indexes = {
        @Index(name = "idx_comment_like_member", columnList = "member_id"),
        @Index(name = "idx_comment_like_comment", columnList = "comment_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike {

    @EmbeddedId
    private CommentLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commentId")
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Builder
    public CommentLike(Member member, Comment comment) {
        this.member = member;
        this.comment = comment;
        this.id = new CommentLikeId(member.getId(), comment.getId());
    }
}

