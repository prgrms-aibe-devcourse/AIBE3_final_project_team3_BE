package triplestar.mixchat.domain.post.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int viewCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    // 필수 필드 null 체크 생성자
    public Post(Member author, String title, String content) {
        this.author = Objects.requireNonNull(author, "author must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.viewCount = 0;
    }

    // 도메인 메서드: 이미지 추가 (엔티티 인스턴스)
    public void addImage(PostImage postImage) {
        Objects.requireNonNull(postImage, "postImage must not be null");
        // 연관관계 편의 메서드: 양방향 설정
        postImage.assignPost(this);
        this.images.add(postImage);
    }

    // 도메인 메서드: 이미지 URL로 바로 추가 (편의 메서드)
    public void addImage(String imageUrl) {
        Objects.requireNonNull(imageUrl, "imageUrl must not be null");
        PostImage image = new PostImage(imageUrl);
        image.assignPost(this);
        this.images.add(image);
    }
}
