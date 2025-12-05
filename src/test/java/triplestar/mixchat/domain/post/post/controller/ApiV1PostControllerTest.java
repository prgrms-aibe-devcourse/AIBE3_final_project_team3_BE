package triplestar.mixchat.domain.post.post.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import triplestar.mixchat.domain.member.member.constant.Role;
import triplestar.mixchat.domain.post.post.dto.CommentCreateReq;
import triplestar.mixchat.domain.post.post.dto.CommentResp;
import triplestar.mixchat.domain.post.post.dto.CommentUpdateReq;
import triplestar.mixchat.domain.post.post.dto.LikeStatusResp;
import triplestar.mixchat.domain.post.post.dto.PostCreateReq;
import triplestar.mixchat.domain.post.post.dto.PostDetailResp;
import triplestar.mixchat.domain.post.post.dto.PostSortType;
import triplestar.mixchat.domain.post.post.dto.PostSummaryResp;
import triplestar.mixchat.domain.post.post.dto.PostUpdateReq;
import triplestar.mixchat.domain.post.post.service.CommentService;
import triplestar.mixchat.domain.post.post.service.PostService;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiV1PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private ApiV1PostController controller;

    private CustomUserDetails user;
    private CustomUserDetails admin;

    @BeforeEach
    void setUp() {
        user = new CustomUserDetails(1L, Role.ROLE_MEMBER);
        admin = new CustomUserDetails(2L, Role.ROLE_ADMIN);
    }

    @Test
    void createPost_callsServiceAndReturnsResponse() {
        String title = "제목";
        String content = "내용";
        PostCreateReq req = new PostCreateReq(title, content);
        PostDetailResp resp = mock(PostDetailResp.class);
        when(postService.createPost(user.getId(), req, null)).thenReturn(resp);

        CustomResponse<PostDetailResp> response = controller.createPost(user, title, content, null);

        assertEquals("게시글 작성 성공", response.msg());
        assertSame(resp, response.data());
        verify(postService).createPost(user.getId(), req, null);
    }

    @Test
    void getPosts_returnsPage() {
        Page<PostSummaryResp> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(postService.getPosts(PostSortType.LATEST, PageRequest.of(0, 20))).thenReturn(page);

        CustomResponse<Page<PostSummaryResp>> response = controller.getPosts("LATEST", 0, 20);

        assertEquals("게시글 목록 조회 성공", response.msg());
        assertSame(page, response.data());
        verify(postService).getPosts(PostSortType.LATEST, PageRequest.of(0, 20));
    }

    @Test
    void getPost_returnsDetail() {
        Long postId = 42L;
        PostDetailResp resp = mock(PostDetailResp.class);
        when(postService.getPostAndIncreaseView(postId)).thenReturn(resp);

        CustomResponse<PostDetailResp> response = controller.getPost(postId);

        assertEquals("게시글 상세 조회 성공", response.msg());
        assertSame(resp, response.data());
        verify(postService).getPostAndIncreaseView(postId);
    }

    @Test
    void updatePost_asUser_updatesAndReturnsUpdated() {
        Long postId = 2L;
        String title = "수정된 제목";
        String content = "수정된 내용";
        PostUpdateReq req = new PostUpdateReq(title, content);
        PostDetailResp resp = mock(PostDetailResp.class);

        when(postService.getPost(postId)).thenReturn(resp);
        // updatePost는 void
        doNothing().when(postService).updatePost(postId, user.getId(), false, req, null);

        CustomResponse<PostDetailResp> response = controller.updatePost(user, postId, title, content, null);

        assertEquals("게시글 수정 성공", response.msg());
        assertSame(resp, response.data());
        verify(postService).updatePost(postId, user.getId(), false, req, null);
        verify(postService).getPost(postId);
    }

    @Test
    void updatePost_asAdmin_updatesWithAdminFlag() {
        Long postId = 3L;
        String title = "관리자 수정 제목";
        String content = "관리자 수정 내용";
        PostUpdateReq req = new PostUpdateReq(title, content);
        PostDetailResp resp = mock(PostDetailResp.class);

        doNothing().when(postService).updatePost(postId, admin.getId(), true, req, null);
        when(postService.getPost(postId)).thenReturn(resp);

        CustomResponse<PostDetailResp> response = controller.updatePost(admin, postId, title, content, null);

        assertEquals("게시글 수정 성공", response.msg());
        assertSame(resp, response.data());
        verify(postService).updatePost(postId, admin.getId(), true, req, null);
    }

    @Test
    void deletePost_deletes() {
        Long postId = 4L;
        doNothing().when(postService).deletePost(postId, user.getId(), false);

        CustomResponse<Void> response = controller.deletePost(user, postId);

        assertEquals("게시글 삭제 성공", response.msg());
        assertNull(response.data());
        verify(postService).deletePost(postId, user.getId(), false);
    }

    @Test
    void deletePost_admin_canDelete() {
        Long postId = 5L;
        doNothing().when(postService).deletePost(postId, admin.getId(), true);

        CustomResponse<Void> response = controller.deletePost(admin, postId);

        assertEquals("게시글 삭제 성공", response.msg());
        verify(postService).deletePost(postId, admin.getId(), true);
    }

    @Test
    void likeAndUnlike_work() {
        Long postId = 6L;
        LikeStatusResp likeResp = mock(LikeStatusResp.class);
        LikeStatusResp unlikeResp = mock(LikeStatusResp.class);

        when(postService.likePost(user.getId(), postId)).thenReturn(likeResp);
        when(postService.unlikePost(user.getId(), postId)).thenReturn(unlikeResp);

        CustomResponse<LikeStatusResp> likeResponse = controller.likePost(user, postId);
        CustomResponse<LikeStatusResp> unlikeResponse = controller.unlikePost(user, postId);

        assertEquals("게시글 좋아요 성공", likeResponse.msg());
        assertSame(likeResp, likeResponse.data());
        assertEquals("게시글 좋아요 취소 성공", unlikeResponse.msg());
        assertSame(unlikeResp, unlikeResponse.data());

        verify(postService).likePost(user.getId(), postId);
        verify(postService).unlikePost(user.getId(), postId);
    }

    @Test
    void getComments_and_createComment() {
        Long postId = 7L;
        List<CommentResp> comments = Collections.emptyList();
        when(commentService.getComments(postId)).thenReturn(comments);

        CustomResponse<List<CommentResp>> listResp = controller.getComments(postId);
        assertEquals("댓글 목록 조회 성공", listResp.msg());
        assertSame(comments, listResp.data());

        CommentCreateReq req = mock(CommentCreateReq.class);
        CommentResp created = mock(CommentResp.class);
        when(commentService.createComment(user.getId(), postId, req)).thenReturn(created);

        CustomResponse<CommentResp> createResp = controller.createComment(user, postId, req);
        assertEquals("댓글 작성 성공", createResp.msg());
        assertSame(created, createResp.data());

        verify(commentService).getComments(postId);
        verify(commentService).createComment(user.getId(), postId, req);
    }

    @Test
    void updateComment_asUser_updatesAndReturnsResponse() {
        Long postId = 8L;
        Long commentId = 1L;
        CommentUpdateReq req = mock(CommentUpdateReq.class);
        CommentResp resp = mock(CommentResp.class);

        when(commentService.updateComment(commentId, user.getId(), false, req)).thenReturn(resp);

        CustomResponse<CommentResp> response = controller.updateComment(user, postId, commentId, req);

        assertEquals("댓글 수정 성공", response.msg());
        assertSame(resp, response.data());
        verify(commentService).updateComment(commentId, user.getId(), false, req);
    }

    @Test
    void updateComment_asAdmin_updatesWithAdminFlag() {
        Long postId = 9L;
        Long commentId = 2L;
        CommentUpdateReq req = mock(CommentUpdateReq.class);
        CommentResp resp = mock(CommentResp.class);

        when(commentService.updateComment(commentId, admin.getId(), true, req)).thenReturn(resp);

        CustomResponse<CommentResp> response = controller.updateComment(admin, postId, commentId, req);

        assertEquals("댓글 수정 성공", response.msg());
        assertSame(resp, response.data());
        verify(commentService).updateComment(commentId, admin.getId(), true, req);
    }

    @Test
    void deleteComment_asUser_deletesComment() {
        Long postId = 10L;
        Long commentId = 3L;

        doNothing().when(commentService).deleteComment(commentId, user.getId(), false);

        CustomResponse<Void> response = controller.deleteComment(user, postId, commentId);

        assertEquals("댓글 삭제 성공", response.msg());
        assertNull(response.data());
        verify(commentService).deleteComment(commentId, user.getId(), false);
    }

    @Test
    void deleteComment_asAdmin_deletesWithAdminFlag() {
        Long postId = 11L;
        Long commentId = 4L;

        doNothing().when(commentService).deleteComment(commentId, admin.getId(), true);

        CustomResponse<Void> response = controller.deleteComment(admin, postId, commentId);

        assertEquals("댓글 삭제 성공", response.msg());
        assertNull(response.data());
        verify(commentService).deleteComment(commentId, admin.getId(), true);
    }

    @Test
    void likeComment_returnsLikeStatus() {
        Long postId = 12L;
        Long commentId = 5L;
        LikeStatusResp likeResp = mock(LikeStatusResp.class);

        when(commentService.likeComment(user.getId(), commentId)).thenReturn(likeResp);

        CustomResponse<LikeStatusResp> response = controller.likeComment(user, postId, commentId);

        assertEquals("댓글 좋아요 성공", response.msg());
        assertSame(likeResp, response.data());
        verify(commentService).likeComment(user.getId(), commentId);
    }

    @Test
    void unlikeComment_returnsUnlikeStatus() {
        Long postId = 13L;
        Long commentId = 6L;
        LikeStatusResp unlikeResp = mock(LikeStatusResp.class);

        when(commentService.unlikeComment(user.getId(), commentId)).thenReturn(unlikeResp);

        CustomResponse<LikeStatusResp> response = controller.unlikeComment(user, postId, commentId);

        assertEquals("댓글 좋아요 취소 성공", response.msg());
        assertSame(unlikeResp, response.data());
        verify(commentService).unlikeComment(user.getId(), commentId);
    }
}

