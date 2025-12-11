package triplestar.mixchat.domain.admin.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import triplestar.mixchat.domain.admin.admin.service.AdminPostService;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.post.post.entity.Post;
import triplestar.mixchat.domain.post.post.repository.PostRepository;
import triplestar.mixchat.testutils.TestMemberFactory;

@SpringBootTest
@ActiveProfiles("test")
public class ApiV1AdminPostControllerTest {
    @Autowired
    private AdminPostService adminPostService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member admin;
    private Member user1;

    private Post post;

    @BeforeEach
    void setUp() {
        admin = memberRepository.save(TestMemberFactory.createAdmin("testAdmin"));
        user1 = memberRepository.save(TestMemberFactory.createMember("testMember"));
    }

    @Test
    @DisplayName("관리자 게시글 삭제 성공")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void 관리자_게시글_삭제_성공() {
        Post post = Post.builder()
                .author(user1)
                .title("post 제목")
                .content("post 내용")
                .build();
        Post saved = postRepository.save(post);

        adminPostService.deletePostByAdmin(post.getId(), admin.getId(), 1);

        boolean exists = postRepository.existsById(post.getId());
        assertThat(exists).isFalse();
    }

    @Test
    void 존재하지_않는_게시글_삭제시_예외발생() {
        Long notExistsId = 999999L;

        assertThatThrownBy(() ->
                adminPostService.deletePostByAdmin(notExistsId, admin.getId(), 1)
        )
                .isInstanceOf(IllegalArgumentException.class);

    }
}
