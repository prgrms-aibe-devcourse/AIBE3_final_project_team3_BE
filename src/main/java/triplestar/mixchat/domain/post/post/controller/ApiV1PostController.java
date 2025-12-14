package triplestar.mixchat.domain.post.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.member.member.constant.Role;
import triplestar.mixchat.domain.post.post.dto.*;
import triplestar.mixchat.domain.post.post.service.CommentService;
import triplestar.mixchat.domain.post.post.service.PostService;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController implements ApiPostController {

    private final PostService postService;
    private final CommentService commentService;

    @Override
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CustomResponse<PostDetailResp> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        Long memberId = userDetails != null ? userDetails.getId() : 1L; // swagger 테스트 편의를 위한 기본값
        PostCreateReq req = new PostCreateReq(title, content);
        PostDetailResp response = postService.createPost(memberId, req, images);
        return CustomResponse.ok("게시글 작성 성공", response);
    }

    @Override
    @GetMapping
    public CustomResponse<Page<PostSummaryResp>> getPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "LATEST") String sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PostSortType sortType;
        try {
            sortType = PostSortType.valueOf(sort.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("정렬 조건은 LATEST 또는 POPULAR만 가능합니다.");
        }

        // page와 size로 Pageable 생성 (정렬은 Repository에서 처리)
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        Long memberId = userDetails != null ? userDetails.getId() : null;
        Page<PostSummaryResp> result = postService.getPosts(memberId, sortType, pageable);
        return CustomResponse.ok("게시글 목록 조회 성공", result);
    }

    @Override
    @GetMapping("/{postId}")
    public CustomResponse<PostDetailResp> getPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        Long memberId = userDetails != null ? userDetails.getId() : null;
        PostDetailResp response = postService.getPostAndIncreaseView(postId, memberId);
        return CustomResponse.ok("게시글 상세 조회 성공", response);
    }

    @Override
    @PatchMapping(value = "/{postId}", consumes = {"multipart/form-data"})
    public CustomResponse<PostDetailResp> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        boolean isAdmin = userDetails.getRole() == Role.ROLE_ADMIN;
        PostUpdateReq req = new PostUpdateReq(title, content);
        postService.updatePost(postId, userDetails.getId(), isAdmin, req, images);
        PostDetailResp response = postService.getPost(postId, userDetails.getId());
        return CustomResponse.ok("게시글 수정 성공", response);
    }

    @Override
    @DeleteMapping("/{postId}")
    public CustomResponse<Void> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        boolean isAdmin = userDetails.getRole() == Role.ROLE_ADMIN;
        postService.deletePost(postId, userDetails.getId(), isAdmin);
        return CustomResponse.ok("게시글 삭제 성공");
    }

    @Override
    @PostMapping("/{postId}/likes")
    public CustomResponse<LikeStatusResp> likePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        LikeStatusResp resp = postService.likePost(userDetails.getId(), postId);
        return CustomResponse.ok("게시글 좋아요 성공", resp);
    }

    @Override
    @DeleteMapping("/{postId}/likes")
    public CustomResponse<LikeStatusResp> unlikePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        LikeStatusResp resp = postService.unlikePost(userDetails.getId(), postId);
        return CustomResponse.ok("게시글 좋아요 취소 성공", resp);
    }

    @Override
    @GetMapping("/{postId}/comments")
    public CustomResponse<List<CommentResp>> getComments(@PathVariable Long postId) {
        List<CommentResp> comments = commentService.getComments(postId);
        return CustomResponse.ok("댓글 목록 조회 성공", comments);
    }

    @Override
    @PostMapping("/{postId}/comments")
    public CustomResponse<CommentResp> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateReq req
    ) {
        CommentResp resp = commentService.createComment(userDetails.getId(), postId, req);
        return CustomResponse.ok("댓글 작성 성공", resp);
    }

    @Override
    @PatchMapping("/{postId}/comments/{commentId}")
    public CustomResponse<CommentResp> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateReq req
    ) {
        boolean isAdmin = userDetails.getRole() == Role.ROLE_ADMIN;
        CommentResp resp = commentService.updateComment(commentId, userDetails.getId(), isAdmin, req);
        return CustomResponse.ok("댓글 수정 성공", resp);
    }

    @Override
    @DeleteMapping("/{postId}/comments/{commentId}")
    public CustomResponse<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        boolean isAdmin = userDetails.getRole() == Role.ROLE_ADMIN;
        commentService.deleteComment(commentId, userDetails.getId(), isAdmin);
        return CustomResponse.ok("댓글 삭제 성공");
    }

    @Override
    @PostMapping("/{postId}/comments/{commentId}/likes")
    public CustomResponse<LikeStatusResp> likeComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        LikeStatusResp resp = commentService.likeComment(userDetails.getId(), commentId);
        return CustomResponse.ok("댓글 좋아요 성공", resp);
    }

    @Override
    @DeleteMapping("/{postId}/comments/{commentId}/likes")
    public CustomResponse<LikeStatusResp> unlikeComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        LikeStatusResp resp = commentService.unlikeComment(userDetails.getId(), commentId);
        return CustomResponse.ok("댓글 좋아요 취소 성공", resp);
    }
}
