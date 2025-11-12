package triplestar.mixchat.domain.post.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

import java.util.Objects;

@Entity
@Getter
@Table(name = "post_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    // 필수 필드 생성자 (null 체크)
    public PostImage(String imageUrl) {
        this.imageUrl = Objects.requireNonNull(imageUrl, "imageUrl must not be null");
    }

    // 연관관계 편의 메서드 (setter 금지 대체)
    void assignPost(Post post) {
        this.post = Objects.requireNonNull(post, "post must not be null");
    }
}