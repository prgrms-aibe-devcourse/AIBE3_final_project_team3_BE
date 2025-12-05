package triplestar.mixchat.domain.post.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_comment_post", columnList = "post_id"),
                @Index(name = "idx_comment_parent", columnList = "parent_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public Comment(Member author, Post post, Comment parent, String content) {
        if (author == null) {
            throw new IllegalArgumentException("작성자는 필수입니다.");
        }
        if (post == null) {
            throw new IllegalArgumentException("게시글은 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
        this.author = author;
        this.post = post;
        this.content = content;
        if (parent != null) {
            this.parent = parent;
            parent.getReplies().add(this);
        }
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
