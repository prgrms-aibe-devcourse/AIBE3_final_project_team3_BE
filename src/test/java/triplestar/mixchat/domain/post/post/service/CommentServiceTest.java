package triplestar.mixchat.domain.post.post.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.post.post.dto.CommentCreateReq;
import triplestar.mixchat.domain.post.post.dto.CommentResp;
import triplestar.mixchat.domain.post.post.dto.CommentUpdateReq;
import triplestar.mixchat.domain.post.post.dto.LikeStatusResp;
import triplestar.mixchat.domain.post.post.dto.PostCreateReq;
import triplestar.mixchat.domain.post.post.dto.PostDetailResp;
import triplestar.mixchat.domain.post.post.repository.CommentLikeRepository;
import triplestar.mixchat.domain.post.post.repository.CommentRepository;
import triplestar.mixchat.testutils.TestMemberFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@DisplayName("댓글 - Comment 서비스")
@Transactional
@SpringBootTest
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private Member member2;
    private Member admin;
    private Long testPostId;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(TestMemberFactory.createMember("test1"));
        member2 = memberRepository.save(TestMemberFactory.createMember("test2"));
        admin = memberRepository.save(TestMemberFactory.createMember("admin"));

        // 테스트용 게시글 생성
        PostCreateReq postReq = new PostCreateReq("테스트 게시글", "테스트 내용");
        PostDetailResp post = postService.createPost(member1.getId(), postReq, null);
        testPostId = post.id();
    }

    @Test
    @DisplayName("댓글 작성 - 성공")
    void createComment_success() {
        // given
        CommentCreateReq req = new CommentCreateReq(null, "테스트 댓글입니다.");

        // when
        CommentResp resp = commentService.createComment(member1.getId(), testPostId, req);

        // then
        assertThat(resp).isNotNull();
        assertThat(resp.content()).isEqualTo("테스트 댓글입니다.");
        assertThat(resp.authorId()).isEqualTo(member1.getId());
        assertThat(resp.authorNickname()).isEqualTo(member1.getNickname());
        assertThat(resp.likeCount()).isEqualTo(0);
        assertThat(resp.replies()).isEmpty();
    }

    @Test
    @DisplayName("댓글 작성 - 존재하지 않는 게시글")
    void createComment_postNotFound_fail() {
        // given
        CommentCreateReq req = new CommentCreateReq(null, "댓글");

        // when & then
        assertThatThrownBy(() -> commentService.createComment(member1.getId(), 99999L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("대댓글 작성 - 성공")
    void createReply_success() {
        // given
        CommentCreateReq parentReq = new CommentCreateReq(null, "부모 댓글");
        CommentResp parent = commentService.createComment(member1.getId(), testPostId, parentReq);

        CommentCreateReq replyReq = new CommentCreateReq(parent.id(), "대댓글입니다.");

        // when
        CommentResp reply = commentService.createComment(member2.getId(), testPostId, replyReq);

        // then
        assertThat(reply.content()).isEqualTo("대댓글입니다.");
        assertThat(reply.authorId()).isEqualTo(member2.getId());

        // 부모 댓글 조회시 대댓글이 포함되어야 함
        List<CommentResp> comments = commentService.getComments(testPostId);
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).replies()).hasSize(1);
        assertThat(comments.get(0).replies().get(0).content()).isEqualTo("대댓글입니다.");
    }

    @Test
    @DisplayName("대댓글 작성 - 다른 게시글의 댓글에 대댓글 실패")
    void createReply_differentPost_fail() {
        // given
        // 다른 게시글 생성
        PostCreateReq otherPostReq = new PostCreateReq("다른 게시글", "내용");
        PostDetailResp otherPost = postService.createPost(member1.getId(), otherPostReq, null);

        // 첫 번째 게시글에 댓글 작성
        CommentCreateReq parentReq = new CommentCreateReq(null, "댓글");
        CommentResp parent = commentService.createComment(member1.getId(), testPostId, parentReq);

        // 두 번째 게시글에 첫 번째 게시글의 댓글을 부모로 하는 대댓글 작성 시도
        CommentCreateReq replyReq = new CommentCreateReq(parent.id(), "대댓글");

        // when & then
        assertThatThrownBy(() ->
                commentService.createComment(member2.getId(), otherPost.id(), replyReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("다른 게시글의 댓글에는 대댓글을 작성할 수 없습니다");
    }

    @Test
    @DisplayName("대댓글 작성 - 3단 댓글 작성 실패")
    void createReply_thirdLevel_fail() {
        // given
        CommentCreateReq parentReq = new CommentCreateReq(null, "부모 댓글");
        CommentResp parent = commentService.createComment(member1.getId(), testPostId, parentReq);

        CommentCreateReq replyReq = new CommentCreateReq(parent.id(), "대댓글");
        CommentResp reply = commentService.createComment(member2.getId(), testPostId, replyReq);

        // 대댓글에 또 대댓글 작성 시도 (3단 댓글)
        CommentCreateReq thirdLevelReq = new CommentCreateReq(reply.id(), "3단 댓글");

        // when & then
        assertThatThrownBy(() ->
                commentService.createComment(member1.getId(), testPostId, thirdLevelReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("댓글은 2단까지만 허용됩니다");
    }

    @Test
    @DisplayName("댓글 목록 조회 - 성공")
    void getComments_success() {
        // given
        CommentCreateReq req1 = new CommentCreateReq(null, "첫 번째 댓글");
        CommentCreateReq req2 = new CommentCreateReq(null, "두 번째 댓글");
        commentService.createComment(member1.getId(), testPostId, req1);
        commentService.createComment(member2.getId(), testPostId, req2);

        // when
        List<CommentResp> comments = commentService.getComments(testPostId);

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).content()).isEqualTo("첫 번째 댓글");
        assertThat(comments.get(1).content()).isEqualTo("두 번째 댓글");
    }

    @Test
    @DisplayName("댓글 목록 조회 - 대댓글 포함")
    void getComments_withReplies_success() {
        // given
        CommentCreateReq parentReq = new CommentCreateReq(null, "부모 댓글");
        CommentResp parent = commentService.createComment(member1.getId(), testPostId, parentReq);

        CommentCreateReq reply1Req = new CommentCreateReq(parent.id(), "대댓글 1");
        CommentCreateReq reply2Req = new CommentCreateReq(parent.id(), "대댓글 2");
        commentService.createComment(member2.getId(), testPostId, reply1Req);
        commentService.createComment(member2.getId(), testPostId, reply2Req);

        // when
        List<CommentResp> comments = commentService.getComments(testPostId);

        // then
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).content()).isEqualTo("부모 댓글");
        assertThat(comments.get(0).replies()).hasSize(2);
        assertThat(comments.get(0).replies().get(0).content()).isEqualTo("대댓글 1");
        assertThat(comments.get(0).replies().get(1).content()).isEqualTo("대댓글 2");
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    void updateComment_success() {
        // given
        CommentCreateReq createReq = new CommentCreateReq(null, "원본 댓글");
        CommentResp created = commentService.createComment(member1.getId(), testPostId, createReq);

        CommentUpdateReq updateReq = new CommentUpdateReq("수정된 댓글");

        // when
        CommentResp updated = commentService.updateComment(
                created.id(), member1.getId(), false, updateReq);

        // then
        assertThat(updated.content()).isEqualTo("수정된 댓글");
        assertThat(updated.id()).isEqualTo(created.id());
    }

    @Test
    @DisplayName("댓글 수정 - 작성자가 아닌 경우 실패")
    void updateComment_notAuthor_fail() {
        // given
        CommentCreateReq createReq = new CommentCreateReq(null, "댓글");
        CommentResp created = commentService.createComment(member1.getId(), testPostId, createReq);

        CommentUpdateReq updateReq = new CommentUpdateReq("수정 시도");

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(created.id(), member2.getId(), false, updateReq))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("권한이 없습니다");
    }

    @Test
    @DisplayName("댓글 수정 - 관리자는 다른 사용자 댓글 수정 가능")
    void updateComment_asAdmin_success() {
        // given
        CommentCreateReq createReq = new CommentCreateReq(null, "댓글");
        CommentResp created = commentService.createComment(member1.getId(), testPostId, createReq);

        CommentUpdateReq updateReq = new CommentUpdateReq("관리자가 수정한 댓글");

        // when
        CommentResp updated = commentService.updateComment(
                created.id(), admin.getId(), true, updateReq);

        // then
        assertThat(updated.content()).isEqualTo("관리자가 수정한 댓글");
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment_success() {
        // given
        CommentCreateReq createReq = new CommentCreateReq(null, "삭제할 댓글");
        CommentResp created = commentService.createComment(member1.getId(), testPostId, createReq);

        // when
        commentService.deleteComment(created.id(), member1.getId(), false);

        // then
        List<CommentResp> comments = commentService.getComments(testPostId);
        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("댓글 삭제 - 대댓글도 함께 삭제")
    void deleteComment_withReplies_success() {
        // given
        CommentCreateReq parentReq = new CommentCreateReq(null, "부모 댓글");
        CommentResp parent = commentService.createComment(member1.getId(), testPostId, parentReq);

        CommentCreateReq replyReq = new CommentCreateReq(parent.id(), "대댓글");
        CommentResp reply = commentService.createComment(member2.getId(), testPostId, replyReq);

        // when
        commentService.deleteComment(parent.id(), member1.getId(), false);

        // then - 댓글 조회 시 예외 발생 또는 없음
        assertThatThrownBy(() -> commentService.getComment(parent.id()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> commentService.getComment(reply.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("댓글 삭제 - 작성자가 아닌 경우 실패")
    void deleteComment_notAuthor_fail() {
        // given
        CommentCreateReq createReq = new CommentCreateReq(null, "댓글");
        CommentResp created = commentService.createComment(member1.getId(), testPostId, createReq);

        // when & then
        assertThatThrownBy(() ->
                commentService.deleteComment(created.id(), member2.getId(), false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("댓글 삭제 - 관리자는 다른 사용자 댓글 삭제 가능")
    void deleteComment_asAdmin_success() {
        // given
        CommentCreateReq createReq = new CommentCreateReq(null, "댓글");
        CommentResp created = commentService.createComment(member1.getId(), testPostId, createReq);

        // when
        commentService.deleteComment(created.id(), admin.getId(), true);

        // then
        List<CommentResp> comments = commentService.getComments(testPostId);
        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("댓글 좋아요 - 성공")
    void likeComment_success() {
        // given
        CommentCreateReq createReq = new CommentCreateReq(null, "댓글");
        CommentResp created = commentService.createComment(member1.getId(), testPostId, createReq);

        // when
        LikeStatusResp resp = commentService.likeComment(member2.getId(), created.id());

        // then
        assertThat(resp.liked()).isTrue();
        assertThat(resp.likeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("댓글 좋아요 - 중복 좋아요 실패")
    void likeComment_duplicate_fail() {
        // given
        CommentCreateReq createReq = new CommentCreateReq(null, "댓글");
        CommentResp created = commentService.createComment(member1.getId(), testPostId, createReq);
        commentService.likeComment(member2.getId(), created.id());

        // when & then
        assertThatThrownBy(() -> commentService.likeComment(member2.getId(), created.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 좋아요를 눌렀습니다");
    }

    @Test
    @DisplayName("댓글 좋아요 취소 - 성공")
    void unlikeComment_success() {
        // given
        CommentCreateReq createReq = new CommentCreateReq(null, "댓글");
        CommentResp created = commentService.createComment(member1.getId(), testPostId, createReq);
        commentService.likeComment(member2.getId(), created.id());

        // when
        LikeStatusResp resp = commentService.unlikeComment(member2.getId(), created.id());

        // then
        assertThat(resp.liked()).isFalse();
        assertThat(resp.likeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("댓글 좋아요 취소 - 좋아요 안된 상태에서 취소 실패")
    void unlikeComment_notLiked_fail() {
        // given
        CommentCreateReq createReq = new CommentCreateReq(null, "댓글");
        CommentResp created = commentService.createComment(member1.getId(), testPostId, createReq);

        // when & then
        assertThatThrownBy(() -> commentService.unlikeComment(member2.getId(), created.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("좋아요가 되어 있지 않습니다");
    }

    @Test
    @DisplayName("댓글 조회 - 좋아요 수 확인")
    void getComments_checkLikeCount() {
        // given
        CommentCreateReq createReq = new CommentCreateReq(null, "댓글");
        CommentResp created = commentService.createComment(member1.getId(), testPostId, createReq);

        // member2와 admin이 좋아요
        commentService.likeComment(member2.getId(), created.id());
        commentService.likeComment(admin.getId(), created.id());

        // when
        List<CommentResp> comments = commentService.getComments(testPostId);

        // then
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).likeCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("대댓글 좋아요 - 성공")
    void likeReply_success() {
        // given
        CommentCreateReq parentReq = new CommentCreateReq(null, "부모 댓글");
        CommentResp parent = commentService.createComment(member1.getId(), testPostId, parentReq);

        CommentCreateReq replyReq = new CommentCreateReq(parent.id(), "대댓글");
        CommentResp reply = commentService.createComment(member2.getId(), testPostId, replyReq);

        // when
        LikeStatusResp likeResp = commentService.likeComment(member1.getId(), reply.id());

        // then
        assertThat(likeResp.liked()).isTrue();
        assertThat(likeResp.likeCount()).isEqualTo(1);

        // 댓글 목록 조회로 확인
        List<CommentResp> comments = commentService.getComments(testPostId);
        assertThat(comments.getFirst().replies().getFirst().likeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("빈 게시글의 댓글 목록 조회")
    void getComments_emptyPost_success() {
        // when
        List<CommentResp> comments = commentService.getComments(testPostId);

        // then
        assertThat(comments).isEmpty();
    }
}

