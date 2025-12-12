package triplestar.mixchat.domain.admin.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import triplestar.mixchat.domain.admin.admin.dto.AdminPostDeleteReq;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.post.post.entity.Post;
import triplestar.mixchat.domain.post.post.repository.PostRepository;
import triplestar.mixchat.testutils.TestMemberFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("관리자 게시글 삭제 테스트")
public class ApiV1AdminPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Member admin;
    private Member user1;

    @BeforeEach
    void setUp() {
        admin = memberRepository.save(
                TestMemberFactory.createAdmin("testAdmin")
        );

        user1 = memberRepository.save(
                TestMemberFactory.createMember("testUser")
        );
    }

    @Test
    @DisplayName("관리자 게시글 삭제 성공")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deletePosts_success() throws Exception {

        Post post = postRepository.save(
                Post.builder()
                        .author(user1)
                        .title("테스트 제목")
                        .content("테스트 내용")
                        .build()
        );

        AdminPostDeleteReq req = new AdminPostDeleteReq(1);

        mockMvc.perform(
                        delete("/api/v1/admin/posts/{postId}", post.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(jsonPath("$.msg").value("게시글 삭제 완료"));

        boolean exists = postRepository.existsById(post.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("관리자 게시글 삭제 실패 - 존재하지 않는 게시글")
    @WithUserDetails(value = "testAdmin", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deletePosts_fail() throws Exception {

        int invalidId = 99;
        AdminPostDeleteReq req = new AdminPostDeleteReq(1);

        mockMvc.perform(
                        delete("/api/v1/admin/posts/{postId}", invalidId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.msg").value("잘못된 요청입니다."));
    }
}