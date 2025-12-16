package triplestar.mixchat.domain.ai.userprompt.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.ai.userprompt.dto.UserPromptReq;
import triplestar.mixchat.domain.ai.userprompt.entity.UserPrompt;
import triplestar.mixchat.domain.ai.userprompt.repository.UserPromptRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.constant.MembershipGrade;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.testutils.TestMemberFactory;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("프롬프트 컨트롤러")
class ApiV1UserPromptControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserPromptRepository userPromptRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private Member premiumMember;
    private Member basicMember;

    @BeforeEach
    void setUp() {
        premiumMember = memberRepository.save(TestMemberFactory.createMember("premiumUser"));
        premiumMember.changeMembershipGrade(MembershipGrade.PREMIUM);
        basicMember = memberRepository.save(TestMemberFactory.createMember("basicUser"));
        basicMember.changeMembershipGrade(MembershipGrade.BASIC);
    }

    @Test
    @DisplayName("프리미엄 회원 프롬프트 생성 성공")
    @WithUserDetails(value = "premiumUser", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPrompt_success() throws Exception {
        UserPromptReq req = new UserPromptReq("테스트 프롬프트", "프롬프트 내용입니다.", "CUSTOM");
        ResultActions result = mvc.perform(post("/api/v1/prompt/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andDo(print());
        result.andExpect(handler().handlerType(ApiV1PromptController.class))
                .andExpect(handler().methodName("create"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("프리미엄 회원 프롬프트 상세 조회 성공")
    @WithUserDetails(value = "premiumUser", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void detailPrompt_success() throws Exception {
        UserPrompt userPrompt = userPromptRepository.save(UserPrompt.create(premiumMember, "상세 프롬프트", "내용", "CUSTOM"));
        ResultActions result = mvc.perform(get("/api/v1/prompt/" + userPrompt.getId()))
                .andDo(print());
        result.andExpect(handler().handlerType(ApiV1PromptController.class))
                .andExpect(handler().methodName("detail"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("프리미엄 회원 프롬프트 수정 성공")
    @WithUserDetails(value = "premiumUser", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updatePrompt_success() throws Exception {
        UserPrompt userPrompt = userPromptRepository.save(UserPrompt.create(premiumMember, "수정 프롬프트", "내용", "CUSTOM"));
        UserPromptReq req = new UserPromptReq("수정된 프롬프트", "수정된 내용입니다.", "CUSTOM");
        ResultActions result = mvc.perform(put("/api/v1/prompt/" + userPrompt.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andDo(print());
        result.andExpect(handler().handlerType(ApiV1PromptController.class))
                .andExpect(handler().methodName("update"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("프리미엄 회원 프롬프트 삭제 성공")
    @WithUserDetails(value = "premiumUser", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deletePrompt_success() throws Exception {
        UserPrompt userPrompt = userPromptRepository.save(UserPrompt.create(premiumMember, "삭제 프롬프트", "내용", "CUSTOM"));
        ResultActions result = mvc.perform(delete("/api/v1/prompt/" + userPrompt.getId()))
                .andDo(print());
        result.andExpect(handler().handlerType(ApiV1PromptController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("기본 회원 프롬프트 상세 조회 실패 - 프리미엄 등급 아님")
    @WithUserDetails(value = "basicUser", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void detailPrompt_fail_noPremium() throws Exception {
        UserPrompt userPrompt = userPromptRepository.save(UserPrompt.create(premiumMember, "상세 프롬프트", "내용", "CUSTOM"));
        ResultActions result = mvc.perform(get("/api/v1/prompt/" + userPrompt.getId()))
                .andDo(print());
        result.andExpect(handler().handlerType(ApiV1PromptController.class))
                .andExpect(handler().methodName("detail"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("기본 회원 프롬프트 생성 실패 - 프리미엄 등급 아님")
    @WithUserDetails(value = "basicUser", userDetailsServiceBeanName = "testUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void createPrompt_fail_noPremium() throws Exception {
        UserPromptReq req = new UserPromptReq("테스트 프롬프트", "프롬프트 내용입니다.", "CUSTOM");
        ResultActions result = mvc.perform(post("/api/v1/prompt/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andDo(print());
        result.andExpect(handler().handlerType(ApiV1PromptController.class))
                .andExpect(handler().methodName("create"))
                .andExpect(status().isForbidden());
    }
}
