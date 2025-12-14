package triplestar.mixchat.domain.post.post.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.post.post.dto.LikeStatusResp;
import triplestar.mixchat.domain.post.post.dto.PostCreateReq;
import triplestar.mixchat.domain.post.post.dto.PostDetailResp;
import triplestar.mixchat.domain.post.post.dto.PostSortType;
import triplestar.mixchat.domain.post.post.dto.PostSummaryResp;
import triplestar.mixchat.domain.post.post.dto.PostUpdateReq;
import triplestar.mixchat.domain.post.post.entity.Post;
import triplestar.mixchat.domain.post.post.entity.PostImage;
import triplestar.mixchat.domain.post.post.entity.PostLike;
import triplestar.mixchat.domain.post.post.repository.PostLikeRepository;
import triplestar.mixchat.domain.post.post.repository.PostRepository;
import triplestar.mixchat.global.s3.S3Uploader;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private static final int MAX_IMAGES = 10;
    private static final String POST_IMAGE_DIR = "posts";

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public PostDetailResp createPost(Long memberId, PostCreateReq req, List<MultipartFile> images) {
        Member author = findMember(memberId);
        Post post = Post.builder()
                .author(author)
                .title(req.title())
                .content(req.content())
                .build();

        uploadAndApplyImages(post, images);
        Post saved = postRepository.save(post);
        return toDetailResp(saved, memberId);
    }

    public Page<PostSummaryResp> getPosts(Long memberId, PostSortType sortType, Pageable pageable) {
        Page<Post> posts;

        if (PostSortType.POPULAR.equals(sortType)) {
            // 인기순: Native Query로 정렬된 결과를 먼저 조회
            Page<Post> popularPosts = postRepository.findPopular(pageable);

            // N+1 방지: EntityGraph로 연관 엔티티 로딩
            List<Long> postIds = popularPosts.getContent().stream()
                    .map(Post::getId)
                    .collect(Collectors.toList());

            if (!postIds.isEmpty()) {
                List<Post> postsWithFetch = postRepository.findByIdsWithFetch(postIds);

                // 원래 정렬 순서 유지
                java.util.Map<Long, Post> postMap = postsWithFetch.stream()
                        .collect(Collectors.toMap(Post::getId, p -> p));

                List<Post> orderedPosts = postIds.stream()
                        .map(postMap::get)
                        .collect(Collectors.toList());

                posts = new org.springframework.data.domain.PageImpl<>(
                        orderedPosts,
                        pageable,
                        popularPosts.getTotalElements()
                );
            } else {
                posts = popularPosts;
            }
        } else {
            // 최신순: FETCH JOIN이 이미 적용되어 있음
            posts = postRepository.findLatest(pageable);
        }

        // Batch로 좋아요 수 한번에 조회하여 성능 최적화
        List<Long> postIds = posts.getContent().stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        java.util.Map<Long, Long> likeCountMap = postIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : postLikeRepository.countByPostIds(postIds).stream()
                        .collect(Collectors.toMap(
                                PostLikeRepository.PostLikeCount::getPostId,
                                PostLikeRepository.PostLikeCount::getLikeCount
                        ));

        // 사용자가 좋아요를 누른 게시글 ID 조회
        java.util.Set<Long> likedPostIds = (memberId != null && !postIds.isEmpty())
                ? java.util.Set.copyOf(postLikeRepository.findLikedPostIdsByMemberIdAndPostIds(memberId, postIds))
                : java.util.Collections.emptySet();

        return posts.map(post -> toSummaryResp(
                post,
                likeCountMap.getOrDefault(post.getId(), 0L).intValue(),
                likedPostIds.contains(post.getId())
        ));
    }

    @Transactional(readOnly = true)
    public PostDetailResp getPost(Long postId, Long memberId) {
        Post post = findPost(postId);
        return toDetailResp(post, memberId);
    }

    @Transactional
    public PostDetailResp getPostAndIncreaseView(Long postId, Long memberId) {
        Post post = findPost(postId);
        post.increaseViewCount();
        return toDetailResp(post, memberId);
    }


    @Transactional
    public void updatePost(Long postId, Long requesterId, boolean isAdmin, PostUpdateReq req, List<MultipartFile> images) {
        Post post = findPost(postId);
        validateAuthorOrAdmin(post, requesterId, isAdmin);

        // 제목과 내용 수정
        post.updateContent(req.title(), req.content());

        // 이미지가 제공된 경우에만 이미지 교체
        if (images != null && !images.isEmpty()) {
            // 기존 이미지 삭제 (S3에서)
            deletePostImages(post);
            // 새 이미지 업로드
            uploadAndApplyImages(post, images);
        }
        // 이미지가 제공되지 않으면 기존 이미지 유지
    }

    @Transactional
    public void deletePost(Long postId, Long requesterId, boolean isAdmin) {
        Post post = findPost(postId);
        validateAuthorOrAdmin(post, requesterId, isAdmin);

        // S3에서 이미지 삭제
        deletePostImages(post);

        postRepository.delete(post);
    }

    @Transactional
    public LikeStatusResp likePost(Long memberId, Long postId) {
        Member member = findMember(memberId);
        Post post = findPost(postId);

        if (postLikeRepository.existsByMemberAndPost(member, post)) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }

        postLikeRepository.save(new PostLike(member, post));
        // DB에서 실제 좋아요 개수를 조회하여 정합성 보장
        int actualLikeCount = postLikeRepository.countByPost(post);
        return new LikeStatusResp(true, actualLikeCount);
    }

    @Transactional
    public LikeStatusResp unlikePost(Long memberId, Long postId) {
        Member member = findMember(memberId);
        Post post = findPost(postId);

        postLikeRepository.findByMemberAndPost(member, post)
                .ifPresentOrElse(like -> {
                    postLikeRepository.delete(like);
                }, () -> {
                    throw new IllegalStateException("좋아요가 되어 있지 않습니다.");
                });

        // DB에서 실제 좋아요 개수를 조회하여 정합성 보장
        int actualLikeCount = postLikeRepository.countByPost(post);
        return new LikeStatusResp(false, actualLikeCount);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    private void validateAuthorOrAdmin(Post post, Long requesterId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (!Objects.equals(post.getAuthor().getId(), requesterId)) {
            throw new AccessDeniedException("게시글에 대한 권한이 없습니다.");
        }
    }

    private void uploadAndApplyImages(Post post, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            post.replaceImages(Collections.emptyList());
            return;
        }

        // 비어있는 파일 제거
        List<MultipartFile> validImages = images.stream()
                .filter(file -> file != null && !file.isEmpty())
                .collect(Collectors.toList());

        if (validImages.isEmpty()) {
            post.replaceImages(Collections.emptyList());
            return;
        }

        if (validImages.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("이미지는 최대 " + MAX_IMAGES + "개까지 업로드 가능합니다.");
        }

        // S3에 업로드하고 URL 받기
        List<PostImage> newImages = validImages.stream()
                .map(file -> {
                    String imageUrl = s3Uploader.uploadFile(file, POST_IMAGE_DIR);
                    return PostImage.builder().imageUrl(imageUrl).build();
                })
                .collect(Collectors.toList());

        post.replaceImages(newImages);
    }

    private void deletePostImages(Post post) {
        if (post.getImages() == null || post.getImages().isEmpty()) {
            return;
        }

        post.getImages().forEach(postImage -> {
            try {
                // S3 URL에서 key 추출하여 삭제
                String imageUrl = postImage.getImageUrl();
                if (imageUrl != null && !imageUrl.isBlank()) {
                    s3Uploader.deleteFileByUrl(extractS3KeyFromUrl(imageUrl));
                }
            } catch (Exception e) {
                // 이미지 삭제 실패해도 계속 진행
                // 로그는 S3Uploader에서 처리
            }
        });
    }

    private String extractS3KeyFromUrl(String url) {
        // URL에서 S3 key 추출
        // 예: https://bucket-name.s3.region.amazonaws.com/posts/uuid.jpg -> posts/uuid.jpg
        try {
            int lastSlashIndex = url.indexOf(POST_IMAGE_DIR);
            if (lastSlashIndex != -1) {
                return url.substring(lastSlashIndex);
            }
            return url;
        } catch (Exception e) {
            return url;
        }
    }

    private PostSummaryResp toSummaryResp(Post post, Integer likeCount, boolean isLiked) {
        int actualLikeCount = (likeCount != null)
                ? likeCount
                : postLikeRepository.countByPostId(post.getId());

        return new PostSummaryResp(
                post.getId(),
                post.getAuthor().getNickname(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrls(),
                post.getViewCount(),
                actualLikeCount,
                isLiked,
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }

    private PostDetailResp toDetailResp(Post post, Long memberId) {
        int actualLikeCount = postLikeRepository.countByPostId(post.getId());
        boolean isLiked = false;
        if (memberId != null) {
            Member member = findMember(memberId);
            isLiked = postLikeRepository.existsByMemberAndPost(member, post);
        }
        return new PostDetailResp(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrls(),
                post.getViewCount(),
                actualLikeCount,
                isLiked,
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}

