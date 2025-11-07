package triplestar.mixchat.domain.member.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.service.AuthService;

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
                //실행처 확인
                .andExpect(handler().handlerType(ApiV1AuthController.class))
                .andExpect(handler().methodName("join"))

                //상태 코드 확인
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원가입 패스워드 확인 불일치")
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
                //실행처 확인
                .andExpect(handler().handlerType(ApiV1AuthController.class))
                .andExpect(handler().methodName("join"))

                //상태 코드 확인
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 응답 - 성공")
    void signIn() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/auth/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "user@example.com",
                                            "password": "ValidPassword123"
                                        }
                                        """)
                )
                .andDo(print());

    }
}