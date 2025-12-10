package triplestar.mixchat.domain.post.post.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.post.post.dto.LikeStatusResp;
import triplestar.mixchat.domain.post.post.dto.PostCreateReq;
import triplestar.mixchat.domain.post.post.dto.PostDetailResp;
import triplestar.mixchat.domain.post.post.dto.PostSortType;
import triplestar.mixchat.domain.post.post.dto.PostSummaryResp;
import triplestar.mixchat.domain.post.post.dto.PostUpdateReq;
import triplestar.mixchat.testutils.TestMemberFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@DisplayName("게시글 - Post 서비스")
@Transactional
@SpringBootTest
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private Member member2;
    private Member admin;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(TestMemberFactory.createMember("test1"));
        member2 = memberRepository.save(TestMemberFactory.createMember("test2"));
        admin = memberRepository.save(TestMemberFactory.createMember("admin"));
    }

    @Test
    @DisplayName("게시글 생성 - 성공")
    void createPost_success() {
        // given
        PostCreateReq req = new PostCreateReq(
                "테스트 제목",
                "테스트 내용입니다."
        );

        // when
        PostDetailResp resp = postService.createPost(member1.getId(), req, null);

        // then
        assertThat(resp).isNotNull();
        assertThat(resp.title()).isEqualTo("테스트 제목");
        assertThat(resp.content()).isEqualTo("테스트 내용입니다.");
        assertThat(resp.authorId()).isEqualTo(member1.getId());
        assertThat(resp.authorNickname()).isEqualTo(member1.getNickname());
        assertThat(resp.viewCount()).isEqualTo(0);
        assertThat(resp.likeCount()).isEqualTo(0);
    }

    // S3 업로드 기능으로 변경되어 주석 처리 (이미지 URL 검증 관련 테스트들은 S3 Mock 필요)
    // @Test
    // @DisplayName("게시글 생성 - 이미지 포함")
    // void createPost_withImages_success() { }
    //
    // @Test
    // @DisplayName("게시글 생성 - 이미지 개수 초과 실패")
    // void createPost_tooManyImages_fail() { }
    //
    // @Test
    // @DisplayName("게시글 생성 - 잘못된 URL 형식 실패")
    // void createPost_invalidUrlFormat_fail() { }

    @Test
    @DisplayName("게시글 목록 조회 - 최신순")
    void getPosts_latest_success() {
        // given
        createSamplePosts();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<PostSummaryResp> result = postService.getPosts(PostSortType.LATEST, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        // 최신순이므로 생성일시 내림차순 확인
        List<PostSummaryResp> posts = result.getContent();
        for (int i = 0; i < posts.size() - 1; i++) {
            assertThat(posts.get(i).createdAt())
                    .isAfterOrEqualTo(posts.get(i + 1).createdAt());
        }
    }

    @Test
    @DisplayName("게시글 목록 조회 - 인기순")
    void getPosts_popular_success() {
        // given
        createSamplePosts();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<PostSummaryResp> result = postService.getPosts(PostSortType.POPULAR, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("게시글 상세 조회 - 성공")
    void getPost_success() {
        // given
        PostCreateReq req = new PostCreateReq("제목", "내용");
        PostDetailResp created = postService.createPost(member1.getId(), req, null);

        // when
        PostDetailResp resp = postService.getPost(created.id());

        // then
        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(created.id());
        assertThat(resp.title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("게시글 상세 조회 - 조회수 증가")
    void getPostAndIncreaseView_success() {
        // given
        PostCreateReq req = new PostCreateReq("제목", "내용");
        PostDetailResp created = postService.createPost(member1.getId(), req, null);

        // when
        PostDetailResp resp1 = postService.getPostAndIncreaseView(created.id());
        PostDetailResp resp2 = postService.getPostAndIncreaseView(created.id());

        // then
        assertThat(resp1.viewCount()).isEqualTo(1);
        assertThat(resp2.viewCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("게시글 상세 조회 - 존재하지 않는 게시글")
    void getPost_notFound_fail() {
        // when & then
        assertThatThrownBy(() -> postService.getPost(99999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    // S3 업로드 기능으로 변경되어 주석 처리 (본문/이미지 수정 성공 케이스는 S3 Mock 필요)
    // @Test
    // @DisplayName("게시글 수정 - 성공")
    // void updatePost_success() { }

    @Test
    @DisplayName("게시글 수정 - 작성자가 아닌 경우 실패")
    void updatePost_notAuthor_fail() {
        // given
        PostCreateReq createReq = new PostCreateReq("제목", "내용");
        PostDetailResp created = postService.createPost(member1.getId(), createReq, null);

        PostUpdateReq updateReq = new PostUpdateReq("수정 제목", "수정 내용");

        // when & then
        assertThatThrownBy(() ->
                postService.updatePost(created.id(), member2.getId(), false, updateReq, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("권한이 없습니다");
    }

    @Test
    @DisplayName("게시글 수정 - 관리자는 다른 사용자 게시글 수정 가능")
    void updatePost_asAdmin_success() {
        // given
        PostCreateReq createReq = new PostCreateReq("제목", "내용");
        PostDetailResp created = postService.createPost(member1.getId(), createReq, null);

        PostUpdateReq updateReq = new PostUpdateReq("관리자 수정 제목", "관리자 수정 내용");

        // when
        postService.updatePost(created.id(), admin.getId(), true, updateReq, null);
        PostDetailResp updated = postService.getPost(created.id());

        // then
        assertThat(updated.title()).isEqualTo("관리자 수정 제목");
        assertThat(updated.content()).isEqualTo("관리자 수정 내용");
    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    void deletePost_success() {
        // given
        PostCreateReq createReq = new PostCreateReq("제목", "내용");
        PostDetailResp created = postService.createPost(member1.getId(), createReq, null);

        // when
        postService.deletePost(created.id(), member1.getId(), false);

        // then
        assertThatThrownBy(() -> postService.getPost(created.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("게시글 삭제 - 작성자가 아닌 경우 실패")
    void deletePost_notAuthor_fail() {
        // given
        PostCreateReq createReq = new PostCreateReq("제목", "내용");
        PostDetailResp created = postService.createPost(member1.getId(), createReq, null);

        // when & then
        assertThatThrownBy(() ->
                postService.deletePost(created.id(), member2.getId(), false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("게시글 삭제 - 관리자는 다른 사용자 게시글 삭제 가능")
    void deletePost_asAdmin_success() {
        // given
        PostCreateReq createReq = new PostCreateReq("제목", "내용");
        PostDetailResp created = postService.createPost(member1.getId(), createReq, null);

        // when
        postService.deletePost(created.id(), admin.getId(), true);

        // then
        assertThatThrownBy(() -> postService.getPost(created.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("게시글 좋아요 - 성공")
    void likePost_success() {
        // given
        PostCreateReq createReq = new PostCreateReq("제목", "내용");
        PostDetailResp created = postService.createPost(member1.getId(), createReq, null);

        // when
        LikeStatusResp resp = postService.likePost(member2.getId(), created.id());

        // then
        assertThat(resp.liked()).isTrue();
        assertThat(resp.likeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시글 좋아요 - 중복 좋아요 실패")
    void likePost_duplicate_fail() {
        // given
        PostCreateReq createReq = new PostCreateReq("제목", "내용");
        PostDetailResp created = postService.createPost(member1.getId(), createReq, null);
        postService.likePost(member2.getId(), created.id());

        // when & then
        assertThatThrownBy(() -> postService.likePost(member2.getId(), created.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 좋아요를 눌렀습니다");
    }

    @Test
    @DisplayName("게시글 좋아요 취소 - 성공")
    void unlikePost_success() {
        // given
        PostCreateReq createReq = new PostCreateReq("제목", "내용");
        PostDetailResp created = postService.createPost(member1.getId(), createReq, null);
        postService.likePost(member2.getId(), created.id());

        // when
        LikeStatusResp resp = postService.unlikePost(member2.getId(), created.id());

        // then
        assertThat(resp.liked()).isFalse();
        assertThat(resp.likeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("게시글 좋아요 취소 - 좋아요 안된 상태에서 취소 실패")
    void unlikePost_notLiked_fail() {
        // given
        PostCreateReq createReq = new PostCreateReq("제목", "내용");
        PostDetailResp created = postService.createPost(member1.getId(), createReq, null);

        // when & then
        assertThatThrownBy(() -> postService.unlikePost(member2.getId(), created.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("좋아요가 되어 있지 않습니다");
    }

    // S3 업로드 기능으로 변경되어 주석 처리 (이미지 수정 성공 케이스)
    // @Test
    // @DisplayName("게시글 이미지 수정 - 이미지 추가")
    // void updatePost_addImages_success() { }
    //
    // @Test
    // @DisplayName("게시글 이미지 수정 - 이미지 삭제")
    // void updatePost_removeImages_success() { }

    private void createSamplePosts() {
        for (int i = 1; i <= 5; i++) {
            PostCreateReq req = new PostCreateReq(
                    "테스트 게시글 " + i,
                    "내용 " + i
            );
            postService.createPost(member1.getId(), req, null);
        }
    }
}

