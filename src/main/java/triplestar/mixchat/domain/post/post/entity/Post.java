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
import java.util.stream.Collectors;

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

    @Builder
    public Post(Member author, String title, String content) {
        if (author == null) {
            throw new IllegalArgumentException("작성자는 필수입니다.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
        this.author = author;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
    }

    public void addImage(PostImage postImage) {
        this.images.add(postImage);
        postImage.assignPost(this);
    }

    public void replaceImages(List<PostImage> newImages) {
        // orphanRemoval이 작동하도록 기존 이미지를 명시적으로 제거
        // clear() 대신 removeIf를 사용하여 각 항목을 하나씩 제거
        this.images.removeIf(image -> true);

        // 새 이미지 추가
        if (newImages != null) {
            newImages.forEach(this::addImage);
        }
    }

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }


    public List<String> getImageUrls() {
        return images.stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());
    }
}
