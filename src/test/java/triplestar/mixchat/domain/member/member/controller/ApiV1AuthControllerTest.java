package triplestar.mixchat.domain.member.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.dto.MemberJoinReq;
import triplestar.mixchat.domain.member.member.dto.SignInResp;
import triplestar.mixchat.domain.member.member.dto.SigninReq;
import triplestar.mixchat.domain.member.member.service.AuthService;
import triplestar.mixchat.testutils.TestHelperController;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("회원 - 인증 컨트롤러")
class ApiV1AuthControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("회원가입 응답 - 성공")
    void join() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/auth/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "user@example.com",
                                            "password": "ValidPassword123",
                                            "passwordConfirm": "ValidPassword123",
                                            "name": "홍길동",
                                            "country": "KR",
                                            "nickname": "믹스마스터",
                                            "englishLevel": "INTERMEDIATE",
                                            "interest": "요리, 여행, 음악",
                                            "description": "안녕하세요. 자기소개입니다."
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1AuthController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("회원가입에 성공했습니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 패스워드 확인 불일치")
    void join_passwordConfirm_fail() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/auth/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "user@example.com",
                                            "password": "ValidPassword123",
                                            "passwordConfirm": "ValidPassword1234",
                                            "name": "홍길동",
                                            "country": "KR",
                                            "nickname": "믹스마스터",
                                            "englishLevel": "INTERMEDIATE",
                                            "interest": "요리, 여행, 음악",
                                            "description": "안녕하세요. 자기소개입니다."
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1AuthController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 응답 - 성공")
    void signIn() throws Exception {
        joinTestData();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/auth/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "test@example.com",
                                            "password": "test1234"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                //실행처 확인
                .andExpect(handler().handlerType(ApiV1AuthController.class))
                .andExpect(handler().methodName("signIn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("로그인에 성공했습니다."));
    }

    private void joinTestData() {
        authService.join(new MemberJoinReq(
                "test@example.com",
                "test1234",
                "test1234",
                "홍길동",
                "KR",
                "믹스마스터",
                EnglishLevel.INTERMEDIATE,
                "요리, 여행, 음악",
                "안녕하세요. 자기소개입니다."
        ));
    }

    @Test
    @DisplayName("로그인 응답 - 실패(아이디 없음)")
    void signIn_Email_Fail() throws Exception {
        joinTestData();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/auth/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "mysecret@example.com",
                                            "password": "test1235"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                //실행처 확인
                .andExpect(handler().handlerType(ApiV1AuthController.class))
                .andExpect(handler().methodName("signIn"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("존재하지 않는 엔티티에 접근했습니다."));
    }

    @Test
    @DisplayName("로그인 응답 - 실패(비밀번호 불일치)")
    void signIn_Password_Fail() throws Exception {
        joinTestData();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/auth/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "test@example.com",
                                            "password": "test1235"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1AuthController.class))
                .andExpect(handler().methodName("signIn"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("잘못된 요청입니다."));
    }

    @Test
    @DisplayName("발급받은 accessToken을 이용해 인증 필터 통과 - 성공")
    void accessToken_filter_success() throws Exception {
        joinTestData();

        SignInResp signInResp = authService.signIn(new SigninReq("test@example.com", "test1234"));
        String accessToken = signInResp.accessToken();

        ResultActions resultActions = mvc
                .perform(
                        get("/test/auth")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(TestHelperController.class))
                .andExpect(handler().methodName("test"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("위조된 액세스토큰 - 실패")
    void accessToken_filter_counterfeit_fail() throws Exception {
        joinTestData();

        SignInResp signInResp = authService.signIn(new SigninReq("test@example.com", "test1234"));
        String accessToken = signInResp.accessToken();

        ResultActions resultActions = mvc
                .perform(
                        get("/test/auth")
                                .header("Authorization", "Bearer " + "abc" + accessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized());
    }
}