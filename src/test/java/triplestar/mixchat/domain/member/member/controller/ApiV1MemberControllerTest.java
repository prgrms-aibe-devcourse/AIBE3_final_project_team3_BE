package triplestar.mixchat.domain.member.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.member.member.service.MemberService;
import triplestar.mixchat.testutils.TestMemberFactory;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("회원 - 정보 컨트롤러")
class ApiV1MemberControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(TestMemberFactory.createMember("user1"));
    }

    @Test
    @DisplayName("내 정보 수정 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void update_info_my_profile_success() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/members/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "업데이트된 이름",
                                            "country": "KR",
                                            "nickname": "새로운닉네임",
                                            "englishLevel": "ADVANCED",
                                            "interests": ["독서", "영화"],
                                            "description": "업데이트된 자기소개입니다."
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("updateMyProfile"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void upload_profile_image_success() throws Exception {
        // 1. 테스트용 파일 생성
        MockMultipartFile testFile = new MockMultipartFile(
                "multipartFile", // 컨트롤러에서 받는 파라미터 이름과 일치해야 함
                "test_profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // 2. MockMvc 요청 수행 (PUT은 MockMvcRequestBuilders.multipart 사용)
        ResultActions resultActions = mvc.perform(
                        multipart("/api/v1/members/profile/image") // PUT 매핑이지만, 파일 업로드는 multipart 사용
                                .file(testFile)
                                .with(request -> { // PUT 메서드를 명시적으로 지정
                                    request.setMethod("PUT");
                                    return request;
                                })
                )
                .andDo(print());

        // 3. 응답 검증
        resultActions.andExpect(status().isOk());

        // 4. DB 검증 (선택적)
        Member updatedMember = memberRepository.findById(member.getId()).get();
        // S3Uploader는 파일명을 UUID로 생성하므로 원본 파일명이 포함되지 않음
        // 따라서 업로드 경로와 확장자 기반으로 검증
        assertThat(updatedMember.getProfileImageUrl())
                .contains("/member/profile/")
                .endsWith(".jpg");
    }

    @Test
    @DisplayName("자신의 상세 조회 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void get_member_profile_success() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/{id}", member.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("getMemberDetail"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("다른 회원의 상세 조회 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void get_other_member_profile_success() throws Exception {
        Member otherMember = memberRepository.save(TestMemberFactory.createMember("user2"));
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/{id}", otherMember.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("getMemberDetail"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비회원의 회원 상세 조회 성공")
    void get_member_profile_as_guest_success() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/{id}", member.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("getMemberDetail"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 목록 조회 성공")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void list_members_success() throws Exception {
        // 여러 회원 생성
        memberRepository.save(TestMemberFactory.createMember("user2"));
        memberRepository.save(TestMemberFactory.createMember("user3"));

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("getMembers"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("")
}