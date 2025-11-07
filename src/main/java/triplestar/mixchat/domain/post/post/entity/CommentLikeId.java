package triplestar.mixchat.domain.post.post.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 좋아요 복합 키 (member_id + comment_id)
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeId implements Serializable {
    private Long memberId;
    private Long commentId;
}

