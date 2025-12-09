package triplestar.mixchat.domain.post.post.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.post.post.dto.CommentCreateReq;
import triplestar.mixchat.domain.post.post.dto.CommentResp;
import triplestar.mixchat.domain.post.post.dto.CommentUpdateReq;
import triplestar.mixchat.domain.post.post.dto.LikeStatusResp;
import triplestar.mixchat.domain.post.post.entity.Comment;
import triplestar.mixchat.domain.post.post.entity.CommentLike;
import triplestar.mixchat.domain.post.post.entity.Post;
import triplestar.mixchat.domain.post.post.repository.CommentLikeRepository;
import triplestar.mixchat.domain.post.post.repository.CommentRepository;
import triplestar.mixchat.domain.post.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public List<CommentResp> getComments(Long postId) {
        Post post = findPost(postId);
        // fetch join으로 댓글과 대댓글을 한번에 조회 (N+1 해결)
        List<Comment> parents = commentRepository.findByPostIdWithReplies(post.getId());

        // 모든 댓글 ID 수집
        List<Long> allCommentIds = new java.util.ArrayList<>();
        for (Comment parent : parents) {
            allCommentIds.add(parent.getId());
            parent.getReplies().forEach(reply -> allCommentIds.add(reply.getId()));
        }

        // Batch로 좋아요 수 한번에 조회
        java.util.Map<Long, Long> likeCountMap = allCommentIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : commentLikeRepository.countByCommentIds(allCommentIds).stream()
                        .collect(Collectors.toMap(
                                CommentLikeRepository.CommentLikeCount::getCommentId,
                                CommentLikeRepository.CommentLikeCount::getLikeCount
                        ));

        return parents.stream()
                .map(parent -> toCommentResp(parent, likeCountMap))
                .collect(Collectors.toList());
    }

    public CommentResp getComment(Long commentId) {
        Comment comment = findComment(commentId);
        return toCommentResp(comment);
    }

    @Transactional
    public CommentResp createComment(Long memberId, Long postId, CommentCreateReq req) {
        Member author = findMember(memberId);
        Post post = findPost(postId);

        Comment parent = null;
        if (req.parentId() != null) {
            parent = findComment(req.parentId());
            validateParent(post, parent);
        }

        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .parent(parent)
                .content(req.content())
                .build();

        Comment saved = commentRepository.save(comment);
        return toCommentResp(saved);
    }

    @Transactional
    public CommentResp updateComment(Long commentId, Long requesterId, boolean isAdmin, CommentUpdateReq req) {
        Comment comment = findComment(commentId);
        validateAuthorOrAdmin(comment, requesterId, isAdmin);
        comment.updateContent(req.content());
        return toCommentResp(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long requesterId, boolean isAdmin) {
        Comment comment = findComment(commentId);
        validateAuthorOrAdmin(comment, requesterId, isAdmin);
        commentRepository.delete(comment);
    }

    @Transactional
    public LikeStatusResp likeComment(Long memberId, Long commentId) {
        Member member = findMember(memberId);
        Comment comment = findComment(commentId);

        if (commentLikeRepository.existsByMemberAndComment(member, comment)) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }

        commentLikeRepository.save(new CommentLike(member, comment));
        // DB에서 실제 좋아요 개수를 조회하여 정합성 보장
        int actualLikeCount = commentLikeRepository.countByComment(comment);
        return new LikeStatusResp(true, actualLikeCount);
    }

    @Transactional
    public LikeStatusResp unlikeComment(Long memberId, Long commentId) {
        Member member = findMember(memberId);
        Comment comment = findComment(commentId);

        commentLikeRepository.findByMemberAndComment(member, comment)
                .ifPresentOrElse(like -> {
                    commentLikeRepository.delete(like);
                }, () -> {
                    throw new IllegalStateException("좋아요가 되어 있지 않습니다.");
                });

        // DB에서 실제 좋아요 개수를 조회하여 정합성 보장
        int actualLikeCount = commentLikeRepository.countByComment(comment);
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

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }

    private void validateParent(Post post, Comment parent) {
        if (!Objects.equals(parent.getPost().getId(), post.getId())) {
            throw new IllegalArgumentException("다른 게시글의 댓글에는 대댓글을 작성할 수 없습니다.");
        }
        if (parent.getParent() != null) {
            throw new IllegalArgumentException("댓글은 2단까지만 허용됩니다.");
        }
    }

    private void validateAuthorOrAdmin(Comment comment, Long requesterId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (!Objects.equals(comment.getAuthor().getId(), requesterId)) {
            throw new AccessDeniedException("댓글에 대한 권한이 없습니다.");
        }
    }

    private CommentResp toCommentResp(Comment comment) {
        return toCommentResp(comment, null);
    }

    private CommentResp toCommentResp(Comment comment, java.util.Map<Long, Long> likeCountMap) {
        List<CommentResp> replies = comment.getReplies().stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(reply -> toCommentResp(reply, likeCountMap))
                .collect(Collectors.toList());

        int likeCount = (likeCountMap != null)
                ? likeCountMap.getOrDefault(comment.getId(), 0L).intValue()
                : commentLikeRepository.countByCommentId(comment.getId());

        return new CommentResp(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getNickname(),
                comment.getContent(),
                likeCount,
                comment.getCreatedAt(),
                comment.getModifiedAt(),
                replies
        );
    }
}
